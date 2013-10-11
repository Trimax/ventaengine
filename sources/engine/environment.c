#include "internalglut.h"

#include "internalcriticalsection.h"
#include "internalresourcemanager.h"
#include "internalscenemanager.h"
#include "internalgraphics.h"
#include "internalsettings.h"
#include "shadermanager.h"
#include "memorymanager.h"
#include "environment.h"

#include <math.h>

/***
 * PURPOSE: Load environment into scene
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] sceneID     - scene identifier to render
 *   PARAM: [IN] sunVelocity - sun oribit velocity
 *   PARAM: [IN] sunDeviation - default sun deviation from (XZ) plane
 *   PARAM: [IN] dayColor    - sky color
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEEnvironmentCreate( const VEUINT sceneID, const VEREAL sunVelocity, const VEREAL sunDeviation, const VECOLOR skyColor )
{
  VESCENE *scene = NULL;
  VEFILE *sphereFile = NULL;

  /* Enter critical section */
  VESectionEnterInternal(p_SettingsGraphicsSection);

  /* Obtain scene */
  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return FALSE;
  }

  /* Terrain already was loaded */
  if (scene->m_Environment)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return FALSE;
  }

  /* Create new environment */
  scene->m_Environment = New(sizeof(VEENVIRONMENT), "Scene environment");
  if (!scene->m_Environment)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return FALSE;
  }

  /* Sphere file not found */
  sphereFile = VEResourcePtrOpen("data/objects/tests/sphere.veo");
  if (!sphereFile)
  {
    Delete(scene->m_Environment);
    scene->m_Environment = NULL;
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return FALSE;
  }

  /* Load sphere file */
  scene->m_Environment->m_Sky = VEObjectLoadInternal(sphereFile->m_Ptr);
  if (!scene->m_Environment->m_Sky)
  {
    VEResourcePtrClose(sphereFile);
    scene->m_Environment = NULL;
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return FALSE;
  }

  /* Setup environment distance */
  scene->m_Environment->m_Sky->m_Scaling = VEVector3D(VE_DEFAULT_FARPLANE - 1.0, VE_DEFAULT_FARPLANE - 1.0, VE_DEFAULT_FARPLANE - 1.0);

  /* Apply environment shader */
  VEObjectApplyProgramInternal(scene->m_Environment->m_Sky, p_SettingsShaderEnvironment[VE_SHADER_PROGRAM]);

  /* Store sky color */
  scene->m_Environment->m_SkyColor = skyColor;

  /* Set default sun velocity */
  scene->m_Environment->m_Sun.m_Velocity  = sunVelocity;
  scene->m_Environment->m_Sun.m_Deviation = sunDeviation;

  /* Close sphere file */
  VEResourcePtrClose(sphereFile);

  /* That's it */
  VESectionLeaveInternal(p_SettingsGraphicsSection);
  return TRUE;
} /* End of 'VEEnvironmentCreate' function */

/***
 * PURPOSE: Render sky spheres
 *   PARAM: [IN] env    - scene environment pointer
 *   PARAM: [IN] camera - current camera pointer
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEEnvironmentRenderInternal( VEENVIRONMENT *env, VECAMERA *camera )
{
  /* Wrong pointers */
  if (!(env && camera))
    return;

  /* Fog parameters */
  glFogf(GL_FOG_DENSITY,                  env->m_Fog.m_Density);
  glFogfv(GL_FOG_COLOR, (const GLfloat *)&env->m_Fog.m_Color);

  /* Render objects */
  VEObjectRender(env->m_Sky);
} /* End of 'VEEnvironmentRenderInternal' function */

/***
 * PURPOSE: Update environment
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEEnvironmentUpdateInternal( VEVOID )
{
  VEREAL sunDistance = (VE_DEFAULT_FARPLANE + 100.0);
  VEREAL sunPitch    = p_SettingsTime * 0.001;
  VESCENE *scene = VESceneGet(p_SettingsGraphicsSceneID);
  if (!scene)
    return;
  if (!scene->m_Environment)
    return;

  /* Apply sun velocity */
  sunPitch *= scene->m_Environment->m_Sun.m_Velocity;

  /* Update sun position */
  scene->m_Environment->m_Sun.m_Position.m_X = sunDistance * cos(scene->m_Environment->m_Sun.m_Deviation) * sin(sunPitch);
  scene->m_Environment->m_Sun.m_Position.m_Y = sunDistance * sin(scene->m_Environment->m_Sun.m_Deviation) * sin(sunPitch);
  scene->m_Environment->m_Sun.m_Position.m_Z = sunDistance *                                                cos(sunPitch);
} /* End of 'VEEnvironmentUpdateInternal' function */
