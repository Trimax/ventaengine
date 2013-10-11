#include "engine/engine.h"

#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <math.h>

/* Light distance & angle */
VEREAL lightDistance = 4.0;
VEREAL lightAngle    = 0.0;

/*** Scene, light, object & camera ***/
VEUINT sceneID  = 0;
VEUINT objectID = 0;
VEUINT cameraID = 0;
VEUINT lightID  = 0;

/* Previous mouse position */
VEUINT mousePrevX = 0;
VEUINT mousePrevY = 0;

/***
 * PURPOSE: Mouse processor
 *   PARAM: [IN] event - mouse event
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID MouseProcessor( VEMOUSE event )
{
  VEINT diffX = event.m_X - mousePrevX;
  VEINT diffY = event.m_Y - mousePrevY;

  if (event.m_Left)
  {
    /* Rotate camera around Y */
    VECameraRotateYaw(sceneID, cameraID, -diffX * 0.5);

    /* Rotate camera around tangage */
    VECameraRotatePitch(sceneID, cameraID, diffY * 0.5);
  }

  if (event.m_Right)
  {
    VEREAL distance = VECameraGetDistance(sceneID, cameraID);
    distance += diffY * 0.5;
    distance = VEMAX(distance, 2.0);
    VECameraSetDistance(sceneID, cameraID, distance);
  }

  /* Store new mouse position */
  mousePrevX = event.m_X;
  mousePrevY = event.m_Y;
} /* End of 'MouseProcessor' function */

/***
 * PURPOSE: Keyboard processor
 *   PARAM: [IN] key - pressed key
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID KeyboardProcessor( VEKEY key )
{
  if (key.m_Code == VE_KEY_ESCAPE)
    VEStop();
} /* End of 'KeyboardProcessor' function */

/***
 * PURPOSE: Update light position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID LightUpdate( VEPOINTER ignored )
{
  while(VEIsRunning())
  {
    lightAngle += 0.01;
    VELightSetPosition(sceneID, lightID, VEVector3D(lightDistance * sin(lightAngle), 2.0, lightDistance * cos(lightAngle)));
    VEWait(10);
  }
} /* End of 'LightUpdate' function */

/***
 * PURPOSE: Main program function
 *  RETURN: Always 0
 *   PARAM: [IN] argc - the number of CL parameters
 *   PARAM: [IN] argv - CL parameters
 *  AUTHOR: Eliseev Dmitry
 ***/
int main( int argc, char *argv[] )
{
  VECONFIGURATION cfg = VEGetConfigurationDefault();
  cfg.m_KeyboardProcessor = KeyboardProcessor;
  cfg.m_MouseProcessor = MouseProcessor;
  cfg.m_EnableVideoSystem = TRUE;
  cfg.m_EnableLogger = FALSE;
  cfg.m_ArgsCount = argc;
  cfg.m_Arguments = argv;
  //cfg.m_VideoScreenWidth  = 1920;
  //cfg.m_VideoScreenHeight = 1080;
  //cfg.m_EnableFullScreen = TRUE;

  /* Configure engine */
  VEConfigure(&cfg);

  /* Initialize Venta engine */
  VEInit();

  /* Is it help screen? */
  if (VEIsArgumentDefined("help"))
  {
    printf("Use: veoviewer -input <filename>\n");
    printf("  input - 3D model in VEO format file name\n");
    VEDeinit();
    exit(0);
  }

  /* There is no model defined */
  if (!VEIsArgumentDefined("input"))
  {
    printf("There are not enough of parameters. See veoviewer -help\n");
    VEDeinit();
    exit(0);
  }

  /* There is no model file name defined */
  if (!VEArgumentGetValue("input"))
  {
    printf("There are not enough of parameters. See veoviewer -help\n");
    VEDeinit();
    exit(0);
  }

  /* Register textures path */
  VEResourceRegisterPath("textures",    "data/textures/environment/stones/");
  VEResourceRegisterPath("textures",    "data/textures/stuff/");
  VEResourceRegisterPath("textures",    "data/textures/world/");
  VEResourceRegisterPath("textures",    "data/textures/tests/");

  /* Load object */
  sceneID = VESceneCreate();
  cameraID = VECameraCreate(sceneID);
  objectID = VEObjectLoad(sceneID, VEArgumentGetValue("input"));

  VEObjectAnimate(sceneID, objectID, 0, 25, 0.5, TRUE);

#if 0
  {
    VEUINT stoneID   = VEObjectLoad(sceneID, "data/objects/environment/stones/stone04.veo");
    VEUINT chestID   = VEObjectLoad(sceneID, "data/objects/tests/tchest_common0.veo");
    VEUINT terrainID = VEObjectLoad(sceneID, "data/objects/world/kingdom_terrain.veo");

    VEObjectSetPosition(sceneID, terrainID, VEVector3D(0.0, -0.1, 0.0));
    VEObjectSetScaling(sceneID, terrainID, VEVector3D(10.0, 20.0, 10.0));

    VEObjectSetPosition(sceneID, chestID, VEVector3D(0.0, -0.3, -1.0));
    VEObjectSetScaling(sceneID, chestID, VEVector3D(0.1, 0.1, 0.1));

    VEObjectSetPosition(sceneID, stoneID, VEVector3D(1.5, -0.3, 0.0));
    VEObjectSetScaling(sceneID, stoneID, VEVector3D(0.4, 0.2, 0.4));
  }
#endif

  /* Create environment */
  VEEnvironmentCreate(sceneID, VE_SUNVELOCITY_DEFAULT, VE_SUNDEVIATION_DEFAULT, VEColor(76, 150, 220, 0));

  /* Light creation */
  lightID = VELightCreate(sceneID);
  VELightSetPosition(sceneID, lightID, VEVector3D(0.0, 0.0, 15.0));

  /* Select camera & scene */
  VECameraSelect(sceneID, cameraID);
  VESceneSelect(sceneID);

  /* Attach camera to object*/
  VECameraSetTarget(sceneID, cameraID, objectID);
  VECameraSetDistance(sceneID, cameraID, 5.0);

  VEThreadCreate(LightUpdate, NULL);

  /* Start viewer process */
  VERun();

  /* Deinitialize Venta engine */
  VEDeinit();
  return 0;
} /* End of 'main' function */
