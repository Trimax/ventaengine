#include "engine/engine.h"

#include <stdlib.h>
#include <stdio.h>
#include <math.h>

/* Light distance & angle */
VEREAL lightDistance = 4.0;
VEREAL lightAngle    = 0.0;

/*** Scene, light, object & camera ***/
VEUINT sceneID  = 0;
VEUINT earthID  = 0;
VEUINT cameraID = 0;
VEUINT lightID  = 0;

VEUINT moonID = 0;

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
    VECameraRotateYaw(sceneID, cameraID, -diffX / 2.0);

    /* Rotate camera around tangage */
    VECameraRotatePitch(sceneID, cameraID, diffY / 2.0);
  }

  if (event.m_Right)
  {
    VEREAL distance = VECameraGetDistance(sceneID, cameraID);
    distance += diffY / 2.0;
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
  cfg.m_EnableMasses = TRUE;
  cfg.m_IsAxesVisible = TRUE;
  //cfg.m_EnableFullScreen = TRUE;

  /* Configure engine */
  VEConfigure(&cfg);

  /* Initialize Venta engine */
  VEInit();

  /* Register textures path */
  VEResourceRegisterPath("textures",    "data/textures/environment/stones/");
  VEResourceRegisterPath("textures",    "data/textures/stuff/");
  VEResourceRegisterPath("textures",    "data/textures/tests/");
  VEResourceRegisterPath("textures",    "data/textures/world/");

  /* Load object */
  sceneID  = VESceneCreate();
  cameraID = VECameraCreate(sceneID);
  earthID = VEObjectLoad(sceneID, "data/objects/world/earth.veo");

  /* Athmosphere */
#if 0
  {
    VEObjectApplyProgram(sceneID, earthID, VEProgramGet(VE_PROGRAM_PLANET));
  }
#endif

  /* Double planet */
#if 1
  {
    VEUINT venusID = VEObjectLoad(sceneID, "data/objects/tests/sphere_d.veo");

    VEObjectSetMass(sceneID, earthID, (VEREAL)10E8);
    VEObjectSetMass(sceneID, venusID, (VEREAL)10E8);

    VEObjectSetPosition(sceneID, earthID, VEVector3D( 2.0, 0.0, 0.0));
    VEObjectSetPosition(sceneID, venusID, VEVector3D(-2.0, 0.0, 0.0));

    VEObjectSetVelocity(sceneID, earthID, VEVector3D(0.0,  0.0608, 0.0));
    VEObjectSetVelocity(sceneID, venusID, VEVector3D(0.0, -0.0608, 0.0));
  }
#endif

  /* Three bodies */
#if 0
  {
    VEUINT venusID = VEObjectLoad(sceneID, "data/objects/world/earth.veo");
    VEUINT plutoID = VEObjectLoad(sceneID, "data/objects/world/earth.veo");

    VEObjectSetMass(sceneID, earthID, 1000.0);
    VEObjectSetMass(sceneID, venusID, (VEREAL)10E8);
    VEObjectSetMass(sceneID, plutoID, (VEREAL)10E8);

    VEObjectSetPosition(sceneID, earthID, VEVector3D(0.0, 2.0, 0.0));
    VEObjectSetPosition(sceneID, venusID, VEVector3D(-2.0, 0.0, 0.0));
    VEObjectSetPosition(sceneID, plutoID, VEVector3D(2.0, 0.0, 0.0));
  }
#endif

  /* Earth and moon */
#if 0
  {
    moonID = VEObjectLoad(sceneID, "data/objects/environment/stones/stone01.veo");
    VEObjectSetMass(sceneID, earthID, 1000000000.0);
    VEObjectSetMass(sceneID, moonID,  1.0);

    VEObjectSetVelocity(sceneID, moonID, VEVector3D(0.0, 0.15, 0.0));
    VEEnvironmentCreate(sceneID, VE_SUNPERIOD_DEFAULT, VECOLOR_BLUE);

    VEObjectSetPosition(sceneID, moonID, VEVector3D(3.0, 0.0, 0.0));
    VEObjectSetScaling(sceneID,  moonID, VEVector3D(0.5, 0.5, 0.5));
  }
#endif

  /* A lot of asteroids */
#if 0
  {
    VEUINT asteroidID = 0;

    for (asteroidID = 0; asteroidID < 200; asteroidID++)
    {
      VEUINT objectID = VEObjectLoad(sceneID, "data/objects/environment/stones/stone04.veo");
      VEObjectSetScaling(sceneID,  objectID, VEVector3D(rand()%20/100.0 + 0.1, rand()%20/100.0 + 0.1, rand()%20/100.0 + 0.1));

      VEObjectSetPosition(sceneID, objectID, VEVector3D(rand()%20 - 10.0, rand()%20 - 10.0, rand()%20 - 10.0));
      VEObjectSetVelocity(sceneID, objectID, VEVector3D(rand()%10 / 1000.0 - 0.005, rand()%10 / 1000.0 - 0.005, rand()%10 / 1000.0 - 0.005));

      VEObjectSetMass(sceneID, objectID, 1.0);
    }
  }

  #endif

  /* Light creation */
  lightID = VELightCreate(sceneID);
  VELightSetPosition(sceneID, lightID, VEVector3D(0.0, 0.0, 2.0));

  /* Select camera & scene */
  VECameraSelect(sceneID, cameraID);
  VESceneSelect(sceneID);

  /* Attach camera to object*/
  VECameraSetTarget(sceneID, cameraID, earthID);
  VECameraSetDistance(sceneID, cameraID, 5.0);

  /* Start viewer process */
  VERun();

  /* Deinitialize Venta engine */
  VEDeinit();
  return 0;
} /* End of 'main' function */
