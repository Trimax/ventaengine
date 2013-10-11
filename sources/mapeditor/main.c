#include "editor.h"

#include <stdio.h>

/***
 * PURPOSE: Prepare editor
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID Constructor( VEVOID )
{
  /* Scene creation */
  sceneID = VESceneCreate();
  VESceneSelect(sceneID);

  /* Register height maps path */
  VEResourceRegisterPath("environment",     "data/environment/");
  VEResourceRegisterPath("heightmaps",      "data/heightmaps/");
  VEResourceRegisterPath("textures",        "data/textures/");
  VEResourceRegisterPath("textures",        "data/textures/world/");
  VEResourceRegisterPath("terraintextures", "data/terrain/");

  /* Camera creation */
  cameraID = VECameraCreate(sceneID);
  VECameraSelect(sceneID, cameraID);

  /* Setup camera */
  VECameraSetPosition(sceneID, cameraID, VEVector3D(5.0, 5.0, 5.0));
  VECameraSetDirection(sceneID, cameraID, VEVector3D(-1.0, -1.0, -1.0));

  lightID = VELightCreate(sceneID);
  VELightSetPosition(sceneID, lightID, VEVector3D(0.0, 15.0, 0.0));
  {
    VELIGHTPARAMETERS params;
    params.m_Ambient   = VECOLOR_WHITE;
    params.m_Diffuse   = VECOLOR_WHITE;
    params.m_Specular  = VECOLOR_WHITE;
    params.m_Shineness = 100;

    VELightSetParameters(sceneID, lightID, params);
  }

  /* Temporary billboard creation */
  VEBillboardCreate(sceneID, VETextureLoad("data/sprites/lens/lens_3.tga"));

  VEObjectLoad(sceneID, "data/objects/world/kingdom_castle01.veo");

  /* GUI creation */
  EditorGUICreate();
} /* End of 'Constructor' function */

/***
 * PURPOSE: Main program function
 *  RETURN: Always 0
 *   PARAM: [IN] argc - the number of CL parameters
 *   PARAM: [IN] argv - CL parameters
 *  AUTHOR: Eliseev Dmitry
 ***/
VEINT main( VEVOID )
{
  VECONFIGURATION cfg = VEGetConfigurationDefault();
  cfg.m_KeyboardProcessor = EditorKeyboardProcessor;
  cfg.m_EnableMemorySystem = TRUE;
  cfg.m_MouseProcessor = EditorMouseProcessor;
  cfg.m_EnableVideoSystem = TRUE;
  cfg.m_IsAxesVisible = TRUE;
  //cfg.m_EnableLogger = TRUE;
  cfg.m_Constructor = Constructor;
  //cfg.m_VideoScreenWidth  = 1900;
  //cfg.m_VideoScreenHeight = 1080;
  //cfg.m_EnableFullScreen = TRUE;

  /* Configure engine */
  VEConfigure(&cfg);

  /* Initialize Venta engine */
  VEInit();

  /* Start main cycle */
  VERun();

  /* Deinitialize Venta engine */
  VEDeinit();

  /* That's it */
  return 0;
} /* End of 'main' function */
