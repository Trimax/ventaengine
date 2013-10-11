#include "internalshadermanager.h"
#include "internalsettings.h"
#include "internalshaders.h"
#include "internalcamera.h"
#include "shadermanager.h"
#include "memorymanager.h"

#ifdef _WIN32
#include <gl/glew.h>
#else
#define GL_GLEXT_PROTOTYPES
#include <GL/gl.h>
#include "gl/glext.h"
#endif

#include <stdio.h>

/***
 * PURPOSE: Create a new shader program using shader's code
 *  RETURN: Program identifier if success, 0 otherwise
 *   PARAM: [IN] type       - shader program type
 *   PARAM: [IN] strShader  - shader code
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEShaderCreateInternal( VEUINT eShaderType, VEBUFFER strShader )
{
  VEUINT shader = glCreateShader(eShaderType);

  /* Create and compile shader */
  glShaderSource(shader, 1, (const GLchar**)&strShader, NULL);
  glCompileShader(shader);

  /* Is shader sucessfully compiled? */
  GLint status;
  glGetShaderiv(shader, GL_COMPILE_STATUS, &status);
  if (status == GL_FALSE)
  {
    GLint infoLogLength;
    glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLogLength);

    GLchar *strInfoLog = New(sizeof(GLchar) * (infoLogLength + 1), "Shader error description");
    glGetShaderInfoLog(shader, infoLogLength, NULL, strInfoLog);

    const char *strShaderType = NULL;
    switch(eShaderType)
    {
      case GL_VERTEX_SHADER:
        strShaderType = "vertex";
        break;

      case GL_GEOMETRY_SHADER:
        strShaderType = "geometry";
        break;

      case GL_FRAGMENT_SHADER:
        strShaderType = "fragment";
        break;
    }

    printf("Compile failure in %s shader:\n%s\n", strShaderType, strInfoLog);
    Delete(strInfoLog);
  }

	return shader;
} /* End of 'VEShaderCreateInternal' function */

/***
 * PURPOSE: Load shader program from file
 *  RETURN: Program identifier if success, 0 otherwise
 *   PARAM: [IN] type       - shader program type
 *   PARAM: [IN] shaderFile - shader file
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEShaderLoadInternal( VEUINT eShaderType, VEBUFFER fileName )
{
  VEBUFFER shaderCode = NULL;
  VEUINT shaderLen = 0, shaderID = 0;

  /* Try to read shader code */
  FILE *shaderFile = fopen(fileName, "rb");
  if (!shaderFile)
    return 0;

  /* Determine shader length */
  fseek(shaderFile, 0, SEEK_END);
  shaderLen = ftell(shaderFile);
  fseek(shaderFile, 0, SEEK_SET);

  /* Memory allocation */
  shaderCode = New(shaderLen+1, "Shader text");
  if (!shaderCode)
  {
    fclose(shaderFile);
    return 0;
  }

  /* Read shader code and close the file */
  fread(shaderCode, 1, shaderLen, shaderFile);
  fclose(shaderFile);

  /* shader program creation */
  shaderID = VEShaderCreateInternal(eShaderType, shaderCode);
  Delete(shaderCode);

  /* That's it */
  return shaderID;
} /* End of 'VEShaderLoadInternal' function */

/***
 * PURPOSE: Create program using shaders
 *  RETURN: Program identifier if success, 0 otherwise
 *   PARAM: [IN] shaders    - array of shaders
 *   PARAM: [IN] numShaders - number of shaders in array
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEProgramCreate( const VEUINT *shaders, const VEUINT numShaders )
{
  VEUINT iLoop = 0;
  VEUINT program = glCreateProgram();

  for(iLoop = 0; iLoop < numShaders; iLoop++)
  	glAttachShader(program, shaders[iLoop]);

  /* Try to link shaders */
  glLinkProgram(program);

  GLint status;
  glGetProgramiv (program, GL_LINK_STATUS, &status);
  if (status == GL_FALSE)
  {
    GLint infoLogLength;
    glGetProgramiv(program, GL_INFO_LOG_LENGTH, &infoLogLength);

    GLchar *strInfoLog = New(sizeof(GLchar) * (infoLogLength + 1), "Program error description");
    glGetProgramInfoLog(program, infoLogLength, NULL, strInfoLog);
    printf("Linker failure: %s\n", strInfoLog);
    Delete(strInfoLog);
  }

  /* That's it */
  return program;
} /* End of 'VEProgramCreate' function */

/***
 * PURPOSE: Delete program
 *   PARAM: [IN] programID - created program identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEProgramDelete( const VEUINT programID )
{
  if (programID == 0)
    return;

  glDeleteProgram(programID);
} /* End of 'VEProgramDelete' function */

/***
 * PURPOSE: Delete shader
 *   PARAM: [IN] shaderID - created shader identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEShaderDelete( const VEUINT shaderID )
{
  if (shaderID == 0)
    return;

  glDeleteShader(shaderID);
} /* End of 'VEShaderDelete' function */

/***
 * PURPOSE: Set texture to specified unit at GPU
 *   PARAM: [IN] textureUnitID - one of GL_TEXTUREXXX definitions
 *   PARAM: [IN] textureID     - loaded texture OpenGL identifier
 *   PARAM: [IN] channelFlagID - uniform variable with texture flag location
 *   PARAM: [IN] channelID     - uniform variable with texture location
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEProgramSetTextureInternal( const VEUINT textureUnitID, const VEUINT textureID, const VEINT channelFlagID, const VEINT channelID )
{
  /* Setup current texture to selected unit */
  glActiveTexture(textureUnitID);
  glBindTexture(GL_TEXTURE_2D, textureID);

  /* Setup texture parameters */
  if (textureID != 0)
  {
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_EXT);
    glTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_RGB_EXT, GL_REPLACE);
  }

  /* Setup texture unit identifier to texture uniform variable */
  if (channelID != -1)
    glUniform1i(channelID, textureUnitID - GL_TEXTURE0);

  /* Setup channel activity flag */
  if (channelFlagID != -1)
    glUniform1i(channelFlagID, textureID != 0);
} /* End of 'VEProgramSetTextureInternal' function */

/***
 * PURPOSE: Use program and bind uniform parameters
 *   PARAM: [IN] mesh - pointer to mesh
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEProgramUseInternal( const VEMESH *mesh )
{
  VESCENE *scene = VESceneGet(p_SettingsGraphicsSceneID);
  if (!(scene&&mesh))
    return;

  /* Select program */
  glUseProgram(mesh->m_ProgramID);
  if (!mesh->m_ProgramID)
    return;

  /*** Setup mesh material and maps ***/
  if (mesh->m_Material)
  {
    VEProgramSetTextureInternal(GL_TEXTURE0, mesh->m_Material->m_MapDiffuse,    mesh->m_ShaderParameters.m_MapDiffuseFlag,    mesh->m_ShaderParameters.m_MapDiffuse);
    VEProgramSetTextureInternal(GL_TEXTURE1, mesh->m_Material->m_MapNormals,    mesh->m_ShaderParameters.m_MapNormalsFlag,    mesh->m_ShaderParameters.m_MapNormals);
    VEProgramSetTextureInternal(GL_TEXTURE2, mesh->m_Material->m_MapReflection, mesh->m_ShaderParameters.m_MapReflectionFlag, mesh->m_ShaderParameters.m_MapReflection);
    VEProgramSetTextureInternal(GL_TEXTURE3, mesh->m_Material->m_MapOpacity,    mesh->m_ShaderParameters.m_MapOpacityFlag,    mesh->m_ShaderParameters.m_MapOpacity);
  } else
  {
    VEProgramSetTextureInternal(GL_TEXTURE0, 0, mesh->m_ShaderParameters.m_MapDiffuseFlag,    mesh->m_ShaderParameters.m_MapDiffuse);
    VEProgramSetTextureInternal(GL_TEXTURE1, 0, mesh->m_ShaderParameters.m_MapNormalsFlag,    mesh->m_ShaderParameters.m_MapNormals);
    VEProgramSetTextureInternal(GL_TEXTURE2, 0, mesh->m_ShaderParameters.m_MapReflectionFlag, mesh->m_ShaderParameters.m_MapReflection);
    VEProgramSetTextureInternal(GL_TEXTURE3, 0, mesh->m_ShaderParameters.m_MapOpacityFlag,    mesh->m_ShaderParameters.m_MapOpacity);
  }

  /*** Common parameters ***/
  { /* Setup camera */
    VECAMERA *camera = VEInternalContainerGet(scene->m_Cameras, scene->m_CameraID);
    if (camera)
      if (mesh->m_ShaderParameters.m_CameraPosition != -1)
        glUniform4f(mesh->m_ShaderParameters.m_CameraPosition, camera->m_Position.m_X, camera->m_Position.m_Y, camera->m_Position.m_Z, 1.f);
  }

  /* Setup sun position and sky color */
  if (scene->m_Environment)
  {
    if (mesh->m_ShaderParameters.m_SunPosition != -1)
      glUniform3f(mesh->m_ShaderParameters.m_SunPosition, scene->m_Environment->m_Sun.m_Position.m_X, scene->m_Environment->m_Sun.m_Position.m_Y, scene->m_Environment->m_Sun.m_Position.m_Z);

    if (mesh->m_ShaderParameters.m_SkyColor != -1)
    {
      VEVECTOR4D skyColor = VEColorToVector4D(scene->m_Environment->m_SkyColor);
      glUniform4f(mesh->m_ShaderParameters.m_SkyColor, skyColor.m_X, skyColor.m_Y, skyColor.m_Z, skyColor.m_W);
    }
  }

  /* Light states */
  if (mesh->m_ShaderParameters.m_Lights != -1)
    glUniform1iv(mesh->m_ShaderParameters.m_Lights, VE_MAXIMAL_LIGHTS, scene->m_LightStates);
} /* End of 'VEProgramUseInternal' function */

/***
 * PURPOSE: Build predefined shaders
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEShaderInitInternal( VEVOID )
{
  /*** Default Venta Engine rendering shader ****/
  p_SettingsShaderRender[VE_SHADER_VERTEX]   = VEShaderCreateInternal(GL_VERTEX_SHADER,   p_ShaderRenderVertex);
  p_SettingsShaderRender[VE_SHADER_FRAGMENT] = VEShaderCreateInternal(GL_FRAGMENT_SHADER, p_ShaderRenderFragment);
  p_SettingsShaderRender[VE_SHADER_PROGRAM]  = VEProgramCreate(p_SettingsShaderRender, 2);

  /*** Normal mapping ****/
  p_SettingsShaderNormalMapping[VE_SHADER_VERTEX]   = VEShaderCreateInternal(GL_VERTEX_SHADER,   p_ShaderNormalMappingVertex);
  p_SettingsShaderNormalMapping[VE_SHADER_FRAGMENT] = VEShaderCreateInternal(GL_FRAGMENT_SHADER, p_ShaderNormalMappingFragment);
  p_SettingsShaderNormalMapping[VE_SHADER_PROGRAM]  = VEProgramCreate(p_SettingsShaderNormalMapping, 2);

  /*** Planet shader ****/
  p_SettingsShaderPlanet[VE_SHADER_VERTEX]   = VEShaderCreateInternal(GL_VERTEX_SHADER,   p_ShaderPlanetVertex);
  p_SettingsShaderPlanet[VE_SHADER_FRAGMENT] = VEShaderCreateInternal(GL_FRAGMENT_SHADER, p_ShaderPlanetFragment);
  p_SettingsShaderPlanet[VE_SHADER_PROGRAM]  = VEProgramCreate(p_SettingsShaderPlanet, 2);

  /*** Terrain shading ***/
  p_SettingsShaderTerrain[VE_SHADER_VERTEX]   = VEShaderCreateInternal(GL_VERTEX_SHADER,   p_ShaderTerrainVertex);
  p_SettingsShaderTerrain[VE_SHADER_FRAGMENT] = VEShaderCreateInternal(GL_FRAGMENT_SHADER, p_ShaderTerrainFragment);
  p_SettingsShaderTerrain[VE_SHADER_PROGRAM]  = VEProgramCreate(p_SettingsShaderTerrain, 2);

  /*** Cartoon shading ***/
  p_SettingsShaderCartoon[VE_SHADER_VERTEX]   = VEShaderCreateInternal(GL_VERTEX_SHADER,   p_ShaderCartoonVertex);
  p_SettingsShaderCartoon[VE_SHADER_FRAGMENT] = VEShaderCreateInternal(GL_FRAGMENT_SHADER, p_ShaderCartoonFragment);
  p_SettingsShaderCartoon[VE_SHADER_PROGRAM]  = VEProgramCreate(p_SettingsShaderCartoon, 2);

  /*** Environment shading ***/
  p_SettingsShaderEnvironment[VE_SHADER_VERTEX]   = VEShaderCreateInternal(GL_VERTEX_SHADER,   p_ShaderEnvironmentVertex);
  p_SettingsShaderEnvironment[VE_SHADER_FRAGMENT] = VEShaderCreateInternal(GL_FRAGMENT_SHADER, p_ShaderEnvironmentFragment);
  p_SettingsShaderEnvironment[VE_SHADER_PROGRAM]  = VEProgramCreate(p_SettingsShaderEnvironment, 2);

  /*** Billboard shading ***/
  p_SettingsShaderBillboard[VE_SHADER_VERTEX]   = VEShaderCreateInternal(GL_VERTEX_SHADER,   p_ShaderBillboardVertex);
  p_SettingsShaderBillboard[VE_SHADER_FRAGMENT] = VEShaderCreateInternal(GL_FRAGMENT_SHADER, p_ShaderBillboardFragment);
  p_SettingsShaderBillboard[VE_SHADER_PROGRAM]  = VEProgramCreate(p_SettingsShaderBillboard, 2);
} /* End of 'VEShaderInitInternal' function */

/***
 * PURPOSE: Delete predefined shaders
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEShaderDeinitInternal( VEVOID )
{
  /*** Render ***/
  VEProgramDelete(p_SettingsShaderRender[VE_SHADER_PROGRAM]);
  VEShaderDelete(p_SettingsShaderRender[VE_SHADER_FRAGMENT]);
  VEShaderDelete(p_SettingsShaderRender[VE_SHADER_VERTEX]);

  /*** Planet ***/
  VEProgramDelete(p_SettingsShaderPlanet[VE_SHADER_PROGRAM]);
  VEShaderDelete(p_SettingsShaderPlanet[VE_SHADER_FRAGMENT]);
  VEShaderDelete(p_SettingsShaderPlanet[VE_SHADER_VERTEX]);

  /*** Cartoon ***/
  VEProgramDelete(p_SettingsShaderCartoon[VE_SHADER_PROGRAM]);
  VEShaderDelete(p_SettingsShaderCartoon[VE_SHADER_FRAGMENT]);
  VEShaderDelete(p_SettingsShaderCartoon[VE_SHADER_VERTEX]);

  /*** Billboards ***/
  VEProgramDelete(p_SettingsShaderBillboard[VE_SHADER_PROGRAM]);
  VEShaderDelete(p_SettingsShaderBillboard[VE_SHADER_FRAGMENT]);
  VEShaderDelete(p_SettingsShaderBillboard[VE_SHADER_VERTEX]);

  /*** Environment ***/
  VEProgramDelete(p_SettingsShaderEnvironment[VE_SHADER_PROGRAM]);
  VEShaderDelete(p_SettingsShaderEnvironment[VE_SHADER_FRAGMENT]);
  VEShaderDelete(p_SettingsShaderEnvironment[VE_SHADER_VERTEX]);

  /*** Terrain mapping ***/
  VEProgramDelete(p_SettingsShaderTerrain[VE_SHADER_PROGRAM]);
  VEShaderDelete(p_SettingsShaderTerrain[VE_SHADER_FRAGMENT]);
  VEShaderDelete(p_SettingsShaderTerrain[VE_SHADER_VERTEX]);

  /*** Normal mapping ***/
  VEProgramDelete(p_SettingsShaderNormalMapping[VE_SHADER_PROGRAM]);
  VEShaderDelete(p_SettingsShaderNormalMapping[VE_SHADER_FRAGMENT]);
  VEShaderDelete(p_SettingsShaderNormalMapping[VE_SHADER_VERTEX]);
} /* End of 'VEShaderDeinitInternal' function */
