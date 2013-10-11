#include "tests.h"

/***
 * PURPOSE: Simple graphics test
 *   PARAM: [IN] scene - scene identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestGraphicsSimple( const VEUINT scene )
{
  VEUINT obj1 = VEObjectLoad(scene, "data/objects/cube/cube.veo");
  VEUINT obj2 = VEObjectLoad(scene, "data/objects/sphere/sphere.veo");
  VEUINT obj3 = VEObjectLoad(scene, "data/objects/teapot/teapot_textured.veo");

  VELIGHTPARAMETERS lightParams;
  VEUINT light1 = VELightCreate(scene), light2 = VELightCreate(scene);

  VELightSetPosition(scene, light1, VEVector3D(10.0, 0.0, 0.0));
  VELightSetPosition(scene, light2, VEVector3D(0.0,  0.0, 10.0));

  lightParams.m_Ambient  = VECOLOR_BLACK;
  lightParams.m_Diffuse  = VECOLOR_RED;
  lightParams.m_Specular = VECOLOR_WHITE;
  VELightSetParameters(scene, light1, lightParams);

  lightParams.m_Ambient  = VECOLOR_BLACK;
  lightParams.m_Diffuse  = VECOLOR_BLUE;
  lightParams.m_Specular = VECOLOR_WHITE;
  VELightSetParameters(scene, light2, lightParams);

  VEObjectSetPosition(scene, obj1, VEVector3D(-5.0, 0.0, 0.0));
  VEObjectSetRotation(scene, obj1, VEVector3D(0.0, 45.38, 0.0));

  VEObjectSetPosition(scene, obj2, VEVector3D(5.0, 0.0, 0.0));

  VEObjectSetPosition(scene, obj3, VEVector3D(0.0, 3.0, 0.0));
  VEObjectSetRotation(scene, obj3, VEVector3D(30.0, 45.38, 60.0));
} /* End of 'TestGraphicsSimple' function */

/***
 * PURPOSE: Textures graphics test
 *   PARAM: [IN] scene - scene identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestGraphicsTextures( const VEUINT scene )
{
  VEUINT light = VELightCreate(scene);
  VEUINT obj = VEObjectLoad(scene, "data/objects/tests/chest.veo");

  VELightSetPosition(scene, light, VEVector3D(2.0, 0.5, 0.0));

  VEObjectSetPosition(scene, obj, VEVector3D(0.0, 2.0, 0.0));
  VEObjectSetRotation(scene, obj, VEVector3D(270.0, 0.0, 0.0));
} /* End of 'TestGraphicsTextures' function */

/***
 * PURPOSE: Cameras graphics test
 *   PARAM: [IN] scene - scene identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestGraphicsCameras( const VEUINT scene )
{
  VEUINT camera = VECameraCreate(scene);

 // VECameraSetTarget(scene, camera, 1);
 // VECameraSetDistance(scene, camera, 25.0);
 // VECameraRotateYaw(scene, camera, 0.3);
 // VECameraRotateTangage(scene, camera, 45.0);

  VECameraSetPosition(scene, camera, VEVector3D(6.0, 6.0, 6.0));
  VECameraSetDirection(scene, camera, VEVector3D(-1.0, -1.0, -1.0));

  VECameraSelect(scene, camera);
} /* End of 'TestGraphicsCameras' function */

/***
 * PURPOSE: Test menu button processor
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestMenuProcessor( VEVOID )
{
  VEConsolePrint("Exit button pressed");
} /* End of 'TestMenuProcessor' function */

/***
 * PURPOSE: Test menu functionality
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestGraphicsMenu( VEVOID )
{
  VEUINT id1 = VEMenuCreate(0, "Hello");
  VEUINT id2 = VEMenuCreate(0, "lalala");
  VEUINT id3 = VEMenuCreate(id2, "thirdlevel");
  VEMenuItemCreate(0, "Exit", TestMenuProcessor);
  VEMenuItemCreate(0, "Test", NULL);

  VEMenuItemCreate(id1, "Super 1", NULL);
  VEMenuItemCreate(id1, "Super 2", NULL);
  VEMenuItemCreate(id1, "Super 3", NULL);

  VEMenuItemCreate(id3, "vr 1", NULL);
  VEMenuItemCreate(id3, "vr 2", NULL);
} /* End of 'TestGraphicsMenu' function */

/***
 * PURPOSE: Graphics test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestGraphics( VEVOID )
{
  VEUINT scene = VESceneCreate();

  /* Register resources */
  VEResourceRegisterPath("textures", "data/textures/");
  VEResourceRegisterPath("textures", "data/heightmaps/");

  /* Graphics tests */
  //TestGraphicsSimple(scene);
  TestGraphicsTextures(scene);
  TestGraphicsCameras(scene);
  TestGraphicsMenu();

  /* Select current renderable scene */
  VESceneSelect(scene);
} /* End of 'TestGraphics' function */
