/* Disable "at exit" hack */
//#define GLUT_DISABLE_ATEXIT_HACK

/* Glut & OpenGL */
#ifdef _WIN32
#include <gl/glew.h>
#else
#include <GL/gl.h>
#include <GL/glext.h>
#endif

#include "internalshadermanager.h"
#include "internalsettings.h"
#include "shadermanager.h"

/***
 * PURPOSE: Select scene to render
 *  RETURN: Program identifier if success, 0 otherwise
 *   PARAM: [IN] type       - shader program type
 *   PARAM: [IN] shaderFile - shader file
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEShaderLoad( const VEBYTE type, const VEBUFFER shaderFile )
{
  if (type == VE_SHADER_FRAGMENT)
    return VEShaderLoadInternal(GL_FRAGMENT_SHADER, shaderFile);
  else if (type == VE_SHADER_VERTEX)
    return VEShaderLoadInternal(GL_VERTEX_SHADER, shaderFile);
  return 0;
} /* End of VEShaderLoad'' function */

/***
 * PURPOSE: Get identifier of predefined program
 *  RETURN: Predefined program identifier if success, 0 otherwise
 *   PARAM: [IN] predefinedProgramType - predefined program type (one of VE_PROGRAM_XXX definitions)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEProgramGet( const VEUINT predefinedProgramType )
{
  /* Determine program type */
  switch(predefinedProgramType)
  {
    case VE_PROGRAM_RENDER:
      return p_SettingsShaderRender[VE_SHADER_PROGRAM];
    case VE_PROGRAM_NORMALMAPPING:
      return p_SettingsShaderNormalMapping[VE_SHADER_PROGRAM];
    case VE_PROGRAM_PLANET:
      return p_SettingsShaderPlanet[VE_SHADER_PROGRAM];
  }

  /* Unknown program */
  return 0;
} /* End of 'VEProgramGet' function */
