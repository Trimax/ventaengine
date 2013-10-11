#include "internalglut.h"

#include "internalscenemanager.h"
#include "internalsettings.h"
#include "memorymanager.h"
#include "internallight.h"
#include "console.h"
#include "light.h"

#include <string.h>

/***
 * PURPOSE: Apply light settings
 *   PARAM: [IN] light - pointer to light to apply
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightApplyInternal( const VELIGHT *light )
{
  if (!light)
    return;

  /* Disable light */
  glDisable(light->m_LightID);

  /* Set light position */
  glLightfv(light->m_LightID, GL_POSITION, (const GLfloat*)&light->m_Position);

  /* Set light direction */
  glLightfv(light->m_LightID, GL_SPOT_DIRECTION, (const GLfloat*)&light->m_Direction);

  /* Set light cutoff */
  glLightf(light->m_LightID, GL_SPOT_CUTOFF, light->m_Cutoff);

  /* Set light color parameters */
  glLightfv(light->m_LightID, GL_AMBIENT,  (const GLfloat*)&light->m_Ambient);
  glLightfv(light->m_LightID, GL_DIFFUSE,  (const GLfloat*)&light->m_Diffuse);
  glLightfv(light->m_LightID, GL_SPECULAR, (const GLfloat*)&light->m_Specular);

  /* Enable/disable lights */
  if (light->m_IsEnabled)
    glEnable(light->m_LightID);
  else
    glDisable(light->m_LightID);
} /* End of 'VELightApply' function */

VELIGHT *VELightCreateInternal( VEVOID )
{
  VELIGHT *light = New(sizeof(VELIGHT), "Light");
  if (!light)
    return NULL;

  /* That's it */
  return light;
} /* End of 'VELightCreateInternal' function */

/***
 * PURPOSE: Create a new light source
 *  RETURN: Created light identifier if success, 0 otherwise
 *   PARAM: [IN] sceneID  - scene identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VELightCreate( const VEUINT sceneID )
{
  VELIGHT *light = NULL;
  VEUINT lightID = 0;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return 0;
  }

  /* To many lights */
  if (scene->m_Lights->m_NumItems > VE_MAXIMAL_LIGHTS)
  {
    VEConsolePrint("Scene has too many light sources");
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return 0;
  }

  /* Try to create light */
  light = VELightCreateInternal();
  if (!light)
  {
    VEConsolePrint("Can't create light");
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return 0;
  }

  /* Enable light */
  light->m_IsEnabled = TRUE;

  /* Add light to scene */
  lightID = VEInternalContainerAdd((VEINTERNALCONTAINER*)scene->m_Lights, light);
  light->m_LightID = GL_LIGHT0 + lightID - 1;

  /* Setup default parameters */
  light->m_Diffuse  = VEColorToVector4D(VECOLOR_WHITE);
  light->m_Ambient  = VEVector4D(0.3, 0.3, 0.3, 1.0);
  light->m_Specular = VEVector4D(0.1, 0.3, 0.1, 1.0);
  light->m_Cutoff   = 90.0;
  light->m_ID       = lightID;

  /* Change light state */
  scene->m_LightStates[light->m_ID] = 1;

  VESectionLeaveInternal(p_SettingsGraphicsSection);

  /* Reset light parameters */
  return lightID;
} /* End of 'VELightCreate' function */

/***
 * PURPOSE: Set light position
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] lightID  - existing light identifier
 *   PARAM: [IN] position - new light position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightSetPosition( const VEUINT sceneID, const VEUINT lightID, const VEVECTOR3D position )
{
  VELIGHT *light = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  light = VEInternalContainerGet(scene->m_Lights, lightID);
  if (light)
  {
    /* Store new light position */
    memcpy(&light->m_Position, &position, sizeof(VEVECTOR3D));
    light->m_Position.m_W = 1.0;
  }

  /* Leave scene's critical section */
  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VELightSetPosition' function */

/***
 * PURPOSE: Set light direction (for spot lights only)
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] lightID   - existing light identifier
 *   PARAM: [IN] direction - new light direction
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightSetDirection( const VEUINT sceneID, const VEUINT lightID, const VEVECTOR3D direction )
{
  VELIGHT *light = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  light = VEInternalContainerGet(scene->m_Lights, lightID);
  if (light)
  {
    /* Store new light direction */
    memcpy(&light->m_Direction, &direction, sizeof(VEVECTOR3D));
    light->m_Position.m_W = 1.0;
  }

  /* Leave scene's critical section */
  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VELightSetDirection' function */

/***
 * PURPOSE: Set light parameters
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] lightID    - existing light identifier
 *   PARAM: [IN] parameters - new light color position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightSetParameters( const VEUINT sceneID, const VEUINT lightID, const VELIGHTPARAMETERS parameters )
{
  VELIGHT *light = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  light = VEInternalContainerGet(scene->m_Lights, lightID);
  if (light)
  {
    /* Set light parameters */
    light->m_Ambient   = VEColorToVector4D(parameters.m_Ambient);
    light->m_Diffuse   = VEColorToVector4D(parameters.m_Diffuse);
    light->m_Specular  = VEColorToVector4D(parameters.m_Specular);
    light->m_Shininess = parameters.m_Shineness;
  }

  /* Leave scene's critical section */
  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VELightSetParameters' function */

/***
 * PURPOSE: Delete an existing light
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] lightID - light identifier to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightDelete( const VEUINT sceneID, const VEUINT lightID )
{
  VELIGHT *light = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  light = VEInternalContainerGet(scene->m_Lights, lightID);
  if (light)
  {
    light->m_Position = VEVector4D(0.0, 0.0, 0.0, 0.0);
    light->m_Diffuse  = VEVector4D(0.0, 0.0, 0.0, 0.0);
    VELightApplyInternal(light);

    /* Change light state */
    scene->m_LightStates[light->m_ID] = 0;
    glDisable(light->m_LightID);

    VEInternalContainerRemove(scene->m_Lights, lightID);
    Delete(light);
  }

  /* Leave scene's critical section */
  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VELightDelete' function */

/***
 * PURPOSE: Render light as sphere
 *   PARAM: [IN] light - pointer to light
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightRenderInternal( const VELIGHT *light )
{
  if (light->m_IsEnabled)
  {
    /* Select color */
    glColor3f(light->m_Diffuse.m_X, light->m_Diffuse.m_Y, light->m_Diffuse.m_Z);

    glPushMatrix();
    glLoadIdentity();

    /* Translate CS */
    glTranslatef(light->m_Position.m_X, light->m_Position.m_Y, light->m_Position.m_Z);
    glutSolidSphere(0.1, 8, 8);

    glPopMatrix();
  }
} /* End of 'VELightRenderInternal' function */

/***
 * PURPOSE: Render all lights of scene as spheres
 *   PARAM: [IN] sceneID - scene identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightRender( const VEUINT sceneID )
{
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
    return;

  /* Render all lights */
  VEInternalContainerForeach(scene->m_Lights, (VEFUNCTION)VELightRenderInternal);
} /* End of 'VELightRender' function */

/***
 * PURPOSE: Enable light
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] lightID    - existing light identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightEnable( const VEUINT sceneID, const VEUINT lightID )
{
  VELIGHT *light = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  light = VEInternalContainerGet(scene->m_Lights, lightID);
  if (light)
  {
    light->m_IsEnabled = TRUE;

    /* Change light state */
    scene->m_LightStates[light->m_ID] = 1;
  }


  /* Leave scene's critical section */
  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VELightEnable' function */

/***
 * PURPOSE: Disable light
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] lightID  - existing light identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightDisable( const VEUINT sceneID, const VEUINT lightID )
{
  VELIGHT *light = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  light = VEInternalContainerGet(scene->m_Lights, lightID);
  if (light)
  {
    light->m_IsEnabled = FALSE;

    /* Change light state */
    scene->m_LightStates[light->m_ID] = 0;
  }

  /* Leave scene's critical section */
  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VELightDisable' function */

/***
 * PURPOSE: Print light information to console
 *   PARAM: [IN] light - light to print information
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightPrint( VELIGHT *light )
{
  VECHAR lightInfo[VE_BUFFER_STANDART];
  memset(lightInfo, 0, VE_BUFFER_STANDART);

  /* Print scene information to console */
  sprintf(lightInfo, "ID: %d; Cone angle: %.2lf; Pos: [%.2lf; %.2lf; %.2lf]; Color: [%.2lf; %.2lf; %.2lf]",
          light->m_ID,
          light->m_Cutoff,
          light->m_Position.m_X, light->m_Position.m_Y, light->m_Position.m_Z,
          light->m_Diffuse.m_X,  light->m_Diffuse.m_Y,  light->m_Diffuse.m_Z);

  VEConsolePrint(lightInfo);
} /* End of 'VELightPrint' function */

/***
 * PURPOSE: Print camera list to console
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightList( VEVOID )
{
  VESCENE *scene = VESceneGet(p_SettingsGraphicsSceneID);
  VEInternalContainerForeach((VEINTERNALCONTAINER*)scene->m_Lights, (VEFUNCTION)VELightPrint);
} /* End of 'VELightList' function */
