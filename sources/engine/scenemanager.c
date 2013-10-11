#include "internalglut.h"
#define VE_GRAPHICS_FRAMERATE 1000



#include <string.h>
#include <stdlib.h>

#include "internalscenemanager.h"
#include "internalenvironment.h"
#include "internalbillboard.h"
#include "internalsettings.h"
#include "internalcamera.h"
#include "internalobject.h"
#include "internallight.h"
#include "memorymanager.h"
#include "scenemanager.h"
#include "console.h"

/* Scenes container */
volatile static VEINTERNALCONTAINER *p_Scenes = NULL;

/***
 * PURPOSE: Initialize scene manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VESceneInit( VEVOID )
{
  if (p_Scenes)
    return TRUE;

  /* Scenes container creation */
  p_Scenes = VEInternalContainerCreate(VE_DEFAULT_CONTAINERSIZE, "Scenes container");
  if (!p_Scenes)
    return FALSE;
  return TRUE;
} /* End of 'VESceneInit' function */

/***
 * PURPOSE: Delete a scene internal
 *   PARAM: [IN] scene - pointer to existing scene
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESceneDeleteInternal( VESCENE *scene )
{
  if (!scene)
    return;

  /* Delete terrain */
  VETerrainDeleteInternal(scene->m_Terrain);

  /* Delete lights */
  VEInternalContainerForeach(scene->m_Lights, Delete);

  /* Delete objects */
  VEInternalContainerForeach(scene->m_Objects, (VEFUNCTION)VEObjectDeleteInternal);

  /* Delete cameras */
  VEInternalContainerForeach(scene->m_Cameras, (VEFUNCTION)Delete);

  /* Delete billboards */
  VEInternalContainerForeach(scene->m_Billboards, (VEFUNCTION)Delete);

  /* Delete containers */
  VEInternalContainerDelete(scene->m_Cameras);
  VEInternalContainerDelete(scene->m_Emitters);
  VEInternalContainerDelete(scene->m_Lights);
  VEInternalContainerDelete(scene->m_Objects);
  VEInternalContainerDelete(scene->m_Billboards);

  /* Delete scene */
  Delete(scene);
} /* End of 'VESceneDeleteInternal' function */

/***
 * PURPOSE: Deinitialize scene manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESceneDeinit( VEVOID )
{
  if (!p_Scenes)
    return;

  /* Set active scene ID to default */
  p_SettingsGraphicsSceneID = 0;

  /* Remove all scenes */
  VEInternalContainerForeach((VEINTERNALCONTAINER*)p_Scenes, (VEFUNCTION)VESceneDeleteInternal);
  VEInternalContainerDelete((VEINTERNALCONTAINER*)p_Scenes);
  p_Scenes = NULL;
} /* End of 'VESceneDeinit' function */

/***
 * PURPOSE: Create a new scene
 *  RETURN: Scene identifier if success, 0 otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VESceneCreate( VEVOID )
{
  VESCENE *scene = NULL;
  if (!p_Scenes)
    return 0;

  /* Create scene object */
  scene = New(sizeof(VESCENE), "Scene");
  if (!scene)
    return 0;

  /* Create cameras container */
  scene->m_Cameras = VEInternalContainerCreate(VE_DEFAULT_CONTAINERSIZE, "Scene cameras");
  if (!scene->m_Cameras)
  {
    VESceneDeleteInternal(scene);
    return 0;
  }

  /* Create emitters container */
  scene->m_Emitters = VEInternalContainerCreate(VE_DEFAULT_CONTAINERSIZE, "Scene emitters");
  if (!scene->m_Emitters)
  {
    VESceneDeleteInternal(scene);
    return 0;
  }

  /* Create lights container */
  scene->m_Lights = VEInternalContainerCreate(VE_DEFAULT_LIGHTS, "Scene lights");
  if (!scene->m_Lights)
  {
    VESceneDeleteInternal(scene);
    return 0;
  }

  /* Create objects container */
  scene->m_Objects = VEInternalContainerCreate(VE_DEFAULT_CONTAINERSIZE, "Scene objects");
  if (!scene->m_Objects)
  {
    VESceneDeleteInternal(scene);
    return 0;
  }

  /* Create billboards container */
  scene->m_Billboards = VEInternalContainerCreate(VE_DEFAULT_CONTAINERSIZE, "Scene billboards");
  if (!scene->m_Billboards)
  {
    VESceneDeleteInternal(scene);
    return 0;
  }

  /* Add scene to container */
  scene->m_ID = VEInternalContainerAdd((VEINTERNALCONTAINER*)p_Scenes, scene);
  return scene->m_ID;
} /* End of VESceneCreate function */

/***
 * PURPOSE: Delete scene
 *   PARAM: [IN] sceneID - scene identifier to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESceneDelete( const VEUINT sceneID )
{
  VESCENE *scene = VEInternalContainerGet((VEINTERNALCONTAINER*)p_Scenes, sceneID);
  if (!scene)
    return;

  /* Waiting for other threads */
  VESectionEnterInternal(p_SettingsGraphicsSection);

  /* If current scene is active, then switch active to default */
  if (sceneID == p_SettingsGraphicsSceneID)
    p_SettingsGraphicsSceneID = 0;

  /* Delete scene */
  VEInternalContainerRemove((VEINTERNALCONTAINER*)p_Scenes, sceneID);
  VESectionLeaveInternal(p_SettingsGraphicsSection);
  VESceneDeleteInternal(scene);
} /* End of 'VESceneDelete' function */

/***
 * PURPOSE: Get scene pointer by it's identifier
 *  RETURN: Scene pointer if success, NULL otherwise
 *   PARAM: [IN] sceneID - scene identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VESCENE *VESceneGet( const VEUINT sceneID )
{
  return VEInternalContainerGet((VEINTERNALCONTAINER*)p_Scenes, sceneID);
} /* End of 'VESceneGet' function */

/***
 * PURPOSE: Update current scene
 *   PARAM: [IN] timeElapsed - time since last frame rendered
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESceneUpdate( const VEREAL timeElapsed )
{
  VESCENE *scene = VEInternalContainerGet((VEINTERNALCONTAINER*)p_Scenes, p_SettingsGraphicsSceneID);
  if (!scene)
    return;

  /* Update objects */
  VEInternalContainerForeachWithArgument(scene->m_Objects, (VEFUNCTION2)VEObjectUpdate, (VEPOINTER)&timeElapsed);
} /* End of 'VESceneUpdate' function */

/***
 * PURPOSE: Render current scene
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESceneRender( VEVOID )
{
  VECAMERA *camera = NULL;
  VESCENE *scene = VEInternalContainerGet((VEINTERNALCONTAINER*)p_Scenes, p_SettingsGraphicsSceneID);
  if (!scene)
    return;

  /* Obtain camera */
  camera = VEInternalContainerGet(scene->m_Cameras, scene->m_CameraID);

  /* Apply lights */
  VEInternalContainerForeach(scene->m_Lights, (VEFUNCTION)VELightApplyInternal);

  /* Render terrain */
  VETerrainRenderInternal(scene->m_Terrain, camera);

  /* Render environment */
  VEEnvironmentRenderInternal(scene->m_Environment, camera);

  /* Render objects */
  VEInternalContainerForeach(scene->m_Objects, (VEFUNCTION)VEObjectRender);

  /* Render billboards */
  VEInternalContainerForeach(scene->m_Billboards, (VEFUNCTION)VEBillboardRender);

  /* Render emitters */

} /* End of 'VESceneRender' function */

/***
 * PURPOSE: Select scene to render
 *   PARAM: [IN] sceneID - scene identifier to render
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESceneSelect( const VEUINT sceneID )
{
  if (sceneID == 0)
    p_SettingsGraphicsSceneID = 0;
  else if (VEInternalContainerGet((VEINTERNALCONTAINER*)p_Scenes, sceneID))
    p_SettingsGraphicsSceneID = sceneID;
} /* End of 'VESceneSelect' function */

/***
 * PURPOSE: Print scene information to console
 *   PARAM: [IN] scene - scene to print information
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEScenePrint( VESCENE *scene )
{
  VECHAR sceneInfo[VE_BUFFER_STANDART];
  memset(sceneInfo, 0, VE_BUFFER_STANDART);

  /* Print scene information to console */
  sprintf(sceneInfo, "ID: %d; Active camera ID: %d; Cameras: %d; Objects: %d; Emitters: %d;", scene->m_ID,
          scene->m_CameraID, scene->m_Cameras->m_NumItems, scene->m_Objects->m_NumItems, scene->m_Emitters->m_NumItems);
  VEConsolePrint(sceneInfo);
} /* End of 'VEScenePrint' function */

/***
 * PURPOSE: Print scenes information to console
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESceneList( VEVOID )
{
  VEInternalContainerForeach((VEINTERNALCONTAINER*)p_Scenes, (VEFUNCTION)VEScenePrint);
} /* End of 'VESceneList' function */

/***
 * PURPOSE: Set current camera parameters
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESceneSetCamera( VEVOID )
{
  VESCENE *scene = VESceneGet(p_SettingsGraphicsSceneID);
  VECAMERA *camera = VEInternalContainerGet(scene->m_Cameras, scene->m_CameraID);

  /* Is camera exist */
  if (!camera)
  {
    gluLookAt(0.0, 0.0, 0.0,
              0.0, 0.0, 1.0,
              0.0, 1.0, 0.0);
    return;
  }

  /* Setup selected camera */
  gluLookAt(camera->m_Position.m_X, camera->m_Position.m_Y, camera->m_Position.m_Z,
            camera->m_LookAt.m_X,   camera->m_LookAt.m_Y,   camera->m_LookAt.m_Z,
            0.0,                    1.0,                    0.0);
} /* End of 'VESceneSetCamera' function */
