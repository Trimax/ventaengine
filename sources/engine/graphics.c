#include "internalglut.h"

/* Self includes */
#include "internalinterfacemanager.h"
#include "internalcriticalsection.h"
#include "internalshadermanager.h"
#include "internalscenemanager.h"
#include "internalenvironment.h"
#include "internalgraphics.h"
#include "internalsettings.h"
#include "internalphysics.h"
#include "internalconsole.h"
#include "internalbuffer.h"
#include "internallight.h"
#include "memorymanager.h"
#include "internaltime.h"
#include "internaltext.h"
#include "internalmenu.h"
#include "internalcore.h"
#include "console.h"
#include "engine.h"

/* System includes */
#include <string.h>
#include <stdio.h>
#include <math.h>

/*** Screen parameters ***/
VEUSHORT p_GraphicsScreenWidth  = VE_DEFAULT_SCREENWIDTH;
VEUSHORT p_GraphicsScreenHeight = VE_DEFAULT_SCREENHEIGHT;

/*** Last frame rendering time ***/
volatile static VEREAL p_GraphicsLastFrameTime = 0.0;

/*** Application argument for dummies :) ***/
static int p_AppArgumentsCount = 0;
static char **p_AppArguments = NULL;

/*** Main window identifier ***/
volatile static VEINT p_GraphicsWindowID = 0;

/*** FPS ***/
static VEUINT p_FPS = 0;

/***
 * PURPOSE: Determine if extension is supported
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] extensionName - name of extension to check
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEGraphicsIsExtensionSupported( VEBUFFER extensionName )
{
  VECHAR extensionNameLocal[VE_BUFFER_LARGE];
	const VEBYTE *pszExtensions = NULL;
	const VEBYTE *pszStart;
	VEBYTE *pszWhere, *pszTerminator;
	VEUINT length = VEBufferLength(extensionName);

	memset(extensionNameLocal, 0, VE_BUFFER_LARGE);
	memcpy(extensionNameLocal, extensionName, length);

	/* Extension names should not have spaces */
	VEBufferRemoveAll(extensionNameLocal, ' ');
	if (VEBufferLength(extensionNameLocal) == 0)
		return FALSE;

	/* Get Extensions String */
	pszExtensions = glGetString(GL_EXTENSIONS);

	/* Search The Extensions String For An Exact Copy */
	pszStart = pszExtensions;
	for(;;)
	{
		pszWhere = (unsigned char *) strstr( (const char *) pszStart, extensionNameLocal );
		if( !pszWhere )
			break;
		pszTerminator = pszWhere + strlen( extensionNameLocal );
		if( pszWhere == pszStart || *( pszWhere - 1 ) == ' ' )
			if( *pszTerminator == ' ' || *pszTerminator == '\0' )
				return TRUE;
		pszStart = pszTerminator;
	}
	return FALSE;
} /* End of 'VEGraphicsIsExtensionSupported' function */

/***
 * PURPOSE: Create fictive CL-arguments
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEParametersCreate( VEVOID )
{
  /* CL arguments */
  p_AppArguments = New(sizeof(char*), "Arguments array initialization");
  if (!p_AppArguments)
    return FALSE;

  /* First argument */
  p_AppArguments[0] = New(256, "First argument in array");
  if (!p_AppArguments[0])
  {
    Delete(p_AppArguments);
    return FALSE;
  }

  /* Storing data */
  memcpy(p_AppArguments[0], "Engine", 6);
  p_AppArgumentsCount = 1;

  /* Rendering critical section */
  p_SettingsGraphicsSection = VESectionCreateInternal("Rendering critical section");
  if (!p_SettingsGraphicsSection)
  {
    Delete(p_AppArguments[0]);
    Delete(p_AppArguments);
    return FALSE;
  }

  /* That's it */
  return TRUE;
} /* End of 'VEParametersCreate' function */

/***
 * PURPOSE: Delete fictive CL-arguments
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEParametersDelete( VEVOID )
{
  /* Remove rendering critical section */
  if (p_SettingsGraphicsSection)
    VESectionDeleteInternal((VECRITICALSECTION*)p_SettingsGraphicsSection);
  p_SettingsGraphicsSection = NULL;

  /* Remove CL arguments */
  if (p_AppArguments)
    Delete(p_AppArguments[0]);
  Delete(p_AppArguments);
  p_AppArguments = NULL;
  p_AppArgumentsCount = 0;
} /* End of 'VEParametersDelete' function */

/***
 * PURPOSE: Setup 2D projection mode
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGraphicsSet2DModeInternal( VEVOID )
{
  glMatrixMode(GL_PROJECTION);
  glLoadIdentity();
  glOrtho(0, p_GraphicsScreenWidth, p_GraphicsScreenHeight, 0, -1, 1);

  glMatrixMode(GL_MODELVIEW);
  glLoadIdentity();
} /* End of 'VEGraphicsSet2DModeInternal' function */

/***
 * PURPOSE: Update frustum planes information
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGraphicsFrustumUpdate( VEVOID )
{
  VEREAL proj[16];
  VEREAL modl[16];
  VEREAL clip[16];
  VEREAL t;

  /* Get current projection matrix */
  glGetFloatv(GL_PROJECTION_MATRIX, proj);

  /* Get current model view matrix */
  glGetFloatv(GL_MODELVIEW_MATRIX, modl);

  /* Multiply matrices */
  clip[ 0] = modl[ 0] * proj[ 0] + modl[ 1] * proj[ 4] + modl[ 2] * proj[ 8] + modl[ 3] * proj[12];
  clip[ 1] = modl[ 0] * proj[ 1] + modl[ 1] * proj[ 5] + modl[ 2] * proj[ 9] + modl[ 3] * proj[13];
  clip[ 2] = modl[ 0] * proj[ 2] + modl[ 1] * proj[ 6] + modl[ 2] * proj[10] + modl[ 3] * proj[14];
  clip[ 3] = modl[ 0] * proj[ 3] + modl[ 1] * proj[ 7] + modl[ 2] * proj[11] + modl[ 3] * proj[15];

  clip[ 4] = modl[ 4] * proj[ 0] + modl[ 5] * proj[ 4] + modl[ 6] * proj[ 8] + modl[ 7] * proj[12];
  clip[ 5] = modl[ 4] * proj[ 1] + modl[ 5] * proj[ 5] + modl[ 6] * proj[ 9] + modl[ 7] * proj[13];
  clip[ 6] = modl[ 4] * proj[ 2] + modl[ 5] * proj[ 6] + modl[ 6] * proj[10] + modl[ 7] * proj[14];
  clip[ 7] = modl[ 4] * proj[ 3] + modl[ 5] * proj[ 7] + modl[ 6] * proj[11] + modl[ 7] * proj[15];

  clip[ 8] = modl[ 8] * proj[ 0] + modl[ 9] * proj[ 4] + modl[10] * proj[ 8] + modl[11] * proj[12];
  clip[ 9] = modl[ 8] * proj[ 1] + modl[ 9] * proj[ 5] + modl[10] * proj[ 9] + modl[11] * proj[13];
  clip[10] = modl[ 8] * proj[ 2] + modl[ 9] * proj[ 6] + modl[10] * proj[10] + modl[11] * proj[14];
  clip[11] = modl[ 8] * proj[ 3] + modl[ 9] * proj[ 7] + modl[10] * proj[11] + modl[11] * proj[15];

  clip[12] = modl[12] * proj[ 0] + modl[13] * proj[ 4] + modl[14] * proj[ 8] + modl[15] * proj[12];
  clip[13] = modl[12] * proj[ 1] + modl[13] * proj[ 5] + modl[14] * proj[ 9] + modl[15] * proj[13];
  clip[14] = modl[12] * proj[ 2] + modl[13] * proj[ 6] + modl[14] * proj[10] + modl[15] * proj[14];
  clip[15] = modl[12] * proj[ 3] + modl[13] * proj[ 7] + modl[14] * proj[11] + modl[15] * proj[15];

  /* Get the planes parameters (Ax + By + Cz + D = 0) */

  /* Find A, B, C, D for right plane */
  p_SettingsGraphicsFrustum[0][0] = clip[ 3] - clip[ 0];
  p_SettingsGraphicsFrustum[0][1] = clip[ 7] - clip[ 4];
  p_SettingsGraphicsFrustum[0][2] = clip[11] - clip[ 8];
  p_SettingsGraphicsFrustum[0][3] = clip[15] - clip[12];

  /* Normalize plane */
  t = 1.0 / sqrt(p_SettingsGraphicsFrustum[0][0] * p_SettingsGraphicsFrustum[0][0] + p_SettingsGraphicsFrustum[0][1] * p_SettingsGraphicsFrustum[0][1] + p_SettingsGraphicsFrustum[0][2] * p_SettingsGraphicsFrustum[0][2]);
  p_SettingsGraphicsFrustum[0][0] *= t;
  p_SettingsGraphicsFrustum[0][1] *= t;
  p_SettingsGraphicsFrustum[0][2] *= t;
  p_SettingsGraphicsFrustum[0][3] *= t;

  /* Find A, B, C, D for left plane */
  p_SettingsGraphicsFrustum[1][0] = clip[ 3] + clip[ 0];
  p_SettingsGraphicsFrustum[1][1] = clip[ 7] + clip[ 4];
  p_SettingsGraphicsFrustum[1][2] = clip[11] + clip[ 8];
  p_SettingsGraphicsFrustum[1][3] = clip[15] + clip[12];

  /* Normalize plane */
  t = 1.0 / sqrt(p_SettingsGraphicsFrustum[1][0] * p_SettingsGraphicsFrustum[1][0] + p_SettingsGraphicsFrustum[1][1] * p_SettingsGraphicsFrustum[1][1] + p_SettingsGraphicsFrustum[1][2] * p_SettingsGraphicsFrustum[1][2]);
  p_SettingsGraphicsFrustum[1][0] *= t;
  p_SettingsGraphicsFrustum[1][1] *= t;
  p_SettingsGraphicsFrustum[1][2] *= t;
  p_SettingsGraphicsFrustum[1][3] *= t;

  /* Find A, B, C, D for bottom plane */
  p_SettingsGraphicsFrustum[2][0] = clip[ 3] + clip[ 1];
  p_SettingsGraphicsFrustum[2][1] = clip[ 7] + clip[ 5];
  p_SettingsGraphicsFrustum[2][2] = clip[11] + clip[ 9];
  p_SettingsGraphicsFrustum[2][3] = clip[15] + clip[13];

  /* Normalize plane */
  t = 1.0 / sqrt(p_SettingsGraphicsFrustum[2][0] * p_SettingsGraphicsFrustum[2][0] + p_SettingsGraphicsFrustum[2][1] * p_SettingsGraphicsFrustum[2][1] + p_SettingsGraphicsFrustum[2][2] * p_SettingsGraphicsFrustum[2][2]);
  p_SettingsGraphicsFrustum[2][0] *= t;
  p_SettingsGraphicsFrustum[2][1] *= t;
  p_SettingsGraphicsFrustum[2][2] *= t;
  p_SettingsGraphicsFrustum[2][3] *= t;

  /* Find A, B, C, D for top plane */
  p_SettingsGraphicsFrustum[3][0] = clip[ 3] - clip[ 1];
  p_SettingsGraphicsFrustum[3][1] = clip[ 7] - clip[ 5];
  p_SettingsGraphicsFrustum[3][2] = clip[11] - clip[ 9];
  p_SettingsGraphicsFrustum[3][3] = clip[15] - clip[13];

  /* Normalize plane */
  t = 1.0 / sqrt(p_SettingsGraphicsFrustum[3][0] * p_SettingsGraphicsFrustum[3][0] + p_SettingsGraphicsFrustum[3][1] * p_SettingsGraphicsFrustum[3][1] + p_SettingsGraphicsFrustum[3][2] * p_SettingsGraphicsFrustum[3][2]);
  p_SettingsGraphicsFrustum[3][0] *= t;
  p_SettingsGraphicsFrustum[3][1] *= t;
  p_SettingsGraphicsFrustum[3][2] *= t;
  p_SettingsGraphicsFrustum[3][3] *= t;

  /* Find A, B, C, D for far plane */
  p_SettingsGraphicsFrustum[4][0] = clip[ 3] - clip[ 2];
  p_SettingsGraphicsFrustum[4][1] = clip[ 7] - clip[ 6];
  p_SettingsGraphicsFrustum[4][2] = clip[11] - clip[10];
  p_SettingsGraphicsFrustum[4][3] = clip[15] - clip[14];

  /* Normalize plane */
  t = 1.0 / sqrt(p_SettingsGraphicsFrustum[4][0] * p_SettingsGraphicsFrustum[4][0] + p_SettingsGraphicsFrustum[4][1] * p_SettingsGraphicsFrustum[4][1] + p_SettingsGraphicsFrustum[4][2] * p_SettingsGraphicsFrustum[4][2]);
  p_SettingsGraphicsFrustum[4][0] *= t;
  p_SettingsGraphicsFrustum[4][1] *= t;
  p_SettingsGraphicsFrustum[4][2] *= t;
  p_SettingsGraphicsFrustum[4][3] *= t;

  /* Find A, B, C, D for far plane */
  p_SettingsGraphicsFrustum[5][0] = clip[ 3] + clip[ 2];
  p_SettingsGraphicsFrustum[5][1] = clip[ 7] + clip[ 6];
  p_SettingsGraphicsFrustum[5][2] = clip[11] + clip[10];
  p_SettingsGraphicsFrustum[5][3] = clip[15] + clip[14];

  /* Normalize plane */
  t = 1.0 / sqrt(p_SettingsGraphicsFrustum[5][0] * p_SettingsGraphicsFrustum[5][0] + p_SettingsGraphicsFrustum[5][1] * p_SettingsGraphicsFrustum[5][1] + p_SettingsGraphicsFrustum[5][2] * p_SettingsGraphicsFrustum[5][2]);
  p_SettingsGraphicsFrustum[5][0] *= t;
  p_SettingsGraphicsFrustum[5][1] *= t;
  p_SettingsGraphicsFrustum[5][2] *= t;
  p_SettingsGraphicsFrustum[5][3] *= t;
} /* End of 'VEGraphicsFrustumUpdate' function ' */

/***
 * PURPOSE: Setup 3D projection mode
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGraphicsSet3DModeInternal( VEVOID )
{
  VEBOOL defaultCamera = TRUE;
  VESCENE *scene = VESceneGet(p_SettingsGraphicsSceneID);
  if (scene)
    if (scene->m_CameraID != 0)
      defaultCamera = FALSE;

  if (p_GraphicsScreenHeight < 1)
    p_GraphicsScreenHeight = 1;
  VEREAL ratio = (VEREAL)p_GraphicsScreenWidth / (VEREAL)p_GraphicsScreenHeight;

  glMatrixMode(GL_PROJECTION);
  glLoadIdentity();
  gluPerspective(40.0, ratio, 1.0, VE_DEFAULT_FARPLANE);

  /* Setup camera */
  if (defaultCamera)
  {
    gluLookAt(0.0, 0.0, 0.0,
              0.0, 0.0, 1.0,
              0.0, 1.0, 0.0);
  } else
    VESceneSetCamera();

  /* Update frustum */
  VEGraphicsFrustumUpdate();

  glMatrixMode(GL_MODELVIEW);
  glLoadIdentity();
} /* End of 'VEGraphicsSet3DModeInternal' function */

/***
 * PURPOSE: Calculate the number of FPS
 *   PARAM: [IN] timeElapsed - time since last frame
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGraphicsFPSInternal( const VEREAL timeElapsed )
{
  p_FPS = 1000.0 / (timeElapsed + 1.0);
} /* End of 'VEGraphicsFPSInternal' function */

/***
 * PURPOSE: Update each object
 *   PARAM: [IN] timeElapsed - time elapsed since last frame rendering (milliseconds)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEUpdate( const VEREAL timeElapsed )
{
  /* Increase elapsed time */
  p_SettingsTime += timeElapsed;

  /* Apply gravitation */
  if (p_SettingsGravityEnabled)
    VEApplyGravity(timeElapsed);

  /* Update active scene */
  VESceneUpdate(timeElapsed);

  /* Update attached cameras position */
  VEUpdateCameras();

  /* Update environment */
  VEEnvironmentUpdateInternal();
} /* End of 'VEUpdate' function */

/***
 * PURPOSE: Render axes
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGraphicsRenderAxes( VEVOID )
{
  glDisable(GL_LIGHTING);
  glBegin(GL_LINES);
    /* X (red) */
    glColor3d(1.0, 0.0, 0.0);

    glVertex3d(0.0,     0.0, 0.0);
    glVertex3d(10000.0, 0.0, 0.0);

    /* Y (green) */
    glColor3d(0.0, 1.0, 0.0);

    glVertex3d(0.0, 0.0,     0.0);
    glVertex3d(0.0, 10000.0, 0.0);

    /* Z (blue) */
    glColor3d(0.0, 0.0, 1.0);

    glVertex3d(0.0, 0.0, 0.0);
    glVertex3d(0.0, 0.0, 10000.0);
  glEnd();
  glEnable(GL_LIGHTING);
} /* End of 'VEGraphicsRenderAxes' function */

/***
 * PURPOSE: Main render function
 *   PARAM: [IN] timeElapsed - time since last render
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGraphicsRenderInternal( const VEREAL timeElapsed )
{
  /* 3D mode rendering*/
  VEGraphicsSet3DModeInternal();
  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  glLoadIdentity();

  /* Axes rendering mode enabled */
  if (p_SettingsGraphicsRenderAxes)
    VEGraphicsRenderAxes();

  /* Lights rendering */
  if (p_SettingsGraphicsRenderLights)
    VELightRender(p_SettingsGraphicsSceneID);

  /* Lights rendering */
  if (p_SettingsGraphicsRenderCameras)
    VECameraRender(p_SettingsGraphicsSceneID);

  /* Rendering current scene */
  VESceneRender();

  /* 2D mode rendering (console, GUI, etc...) */
  VEGraphicsSet2DModeInternal();

  /* GUI rendering */
  if (p_SettingsGraphicsInterfaceID != 0)
    VEInterfaceRenderInternal();

  /* Console rendering (if opened) */
  VEConsoleRenderInternal(p_FPS);

  /* Swap buffers */
  glutSwapBuffers();

  /* Determine FPS number */
  VEGraphicsFPSInternal(timeElapsed);
} /* End of 'VEGraphicsRenderInternal' function */

/***
 * PURPOSE: Executes each time when application need to render next frams
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGraphicsDisplayInternal( VEVOID )
{
  static VEREAL timeAccumulator = 0.0;
  VEREAL timeElapsed = VETimeElapsed();

  /* Venta engine deinitialization */
  if (VEIsStopping())
  {
    VEDeinit();
    glutLeaveMainLoop();
    return;
  }

  /* Render not each frame */
  timeAccumulator += timeElapsed;
  if (timeAccumulator > VE_FRAME_DURATION)
  {
      VESectionEnterInternal((VECRITICALSECTION*)p_SettingsGraphicsSection);
      VEUpdate(timeAccumulator);
      VEGraphicsRenderInternal(timeAccumulator);
      VESectionLeaveInternal((VECRITICALSECTION*)p_SettingsGraphicsSection);

      timeAccumulator = 0.0;
  }
} /* End of 'VEGraphicsDisplayInternal' function */

/***
 * PURPOSE: Executes each time when application has nothing to do
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGraphicsIdleInternal( VEVOID )
{
  glutPostRedisplay();
} /* End of 'VEGraphicsIdleInternal' function */

/***
 * PURPOSE: Resize window function
 *   PARAM: [IN] screenWidth  - render window new screen width
 *   PARAM: [IN] screenHeight - render window new screen height
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGraphicsResizeInternal( const VEINT width, const VEINT height )
{
  p_GraphicsScreenWidth  = width;
  p_GraphicsScreenHeight = height;

  /* Update viewport */
  glViewport(0, 0, width, height);
} /* End of 'VEGraphicsResizeInternal' function */

/***
 * PURPOSE: Initialize graphics engine
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] screenWidth  - render window screen width
 *   PARAM: [IN] screenHeight - render window screen height
 *   PARAM: [IN] fullscreen   - full screen mode flag
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEGraphicsInit( const VEUSHORT screenWidth, const VEUSHORT screenHeight, const VEBOOL fullscreen )
{
  VEUINT planeID = 0;
  if (!VEParametersCreate())
    return FALSE;

  /* GLUT initialization */
  glutInit(&p_AppArgumentsCount, p_AppArguments);
  glutInitDisplayMode(GLUT_DOUBLE|GLUT_RGB|GLUT_DEPTH);

  glutSetOption(GLUT_ACTION_ON_WINDOW_CLOSE, GLUT_ACTION_GLUTMAINLOOP_RETURNS);

  /* Which mode */
  if (fullscreen)
  {
    VECHAR modeString[VE_BUFFER_LARGE];
    memset(modeString, 0, VE_BUFFER_LARGE);
    sprintf(modeString, "%dx%d:32", screenWidth, screenHeight);

    glutGameModeString(modeString);
    glutEnterGameMode();
  } else
  {
    /* Main window creation */
    p_GraphicsWindowID = glutCreateWindow("Venta Engine");
    glutReshapeWindow(screenWidth, screenHeight);
  }

  #ifdef _WIN32
  glewInit();
  #endif

  /* Register rendering function */
  glutDisplayFunc(VEGraphicsDisplayInternal);
  glutIdleFunc(VEGraphicsIdleInternal);
  glutReshapeFunc(VEGraphicsResizeInternal);

  /* Default blending function */
  glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
  glEnable(GL_BLEND);

  /* Enable shading smoothing shading model */
  glShadeModel(GL_SMOOTH);
  glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
  glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

  /* Anti-aliasing */
  glEnable (GL_LINE_SMOOTH);
  glHint (GL_LINE_SMOOTH_HINT, GL_NICEST);
  glLineWidth(2.5);
  glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);
  glEnable(GL_POINT_SMOOTH);

  /* That's it */
  glEnable(GL_DEPTH_TEST);
  glDepthFunc(GL_LEQUAL);
  glEnable(GL_LIGHTING);

  /* Enable fog */
  glFogi(GL_FOG_MODE, GL_EXP);
  glFogf(GL_FOG_DENSITY, 0.0);
  glHint(GL_FOG_HINT, GL_DONT_CARE);
  glFogf(GL_FOG_START, 1.0f);
  glFogf(GL_FOG_END, 5.0f);
  glEnable(GL_FOG);

  /* Enable color materials support */
  glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);
  glEnable(GL_COLOR_MATERIAL);

  /* Reset frustum */
  for (planeID = 0; planeID < 6; planeID++)
    memset(p_SettingsGraphicsFrustum[planeID], 0, 4*sizeof(VEREAL));
  VEConsolePrint("Venta Engine initialized");

  /* Compile and link predefined shaders */
  VEShaderInitInternal();

  /* Check VBO support */
  p_SettingsGraphicsVBOSupported = VEGraphicsIsExtensionSupported("GL_ARB_vertex_buffer_object");
  return TRUE;
} /* End of 'VEGraphicsInit' function */

/***
 * PURPOSE: Start main loop
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VERun( VEVOID )
{
  /* Engine is not initialized */
  if (!p_AppArguments)
    return;

  /* Prepare menu */
  VEMenuPrepare();

  /* Enter main loop */
  glutMainLoop();
} /* End of 'VERun' function */

/***
 * PURPOSE: Determine graphics engine state
 *  RETURN: TRUE if engine initialized, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEGraphicsIsInitialized( VEVOID )
{
  return p_AppArguments != NULL;
} /* End of 'VEGraphicsIsInitialized' function */

/***
 * PURPOSE: Deinitialize graphics engine
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGraphicsDeinit( VEVOID )
{
  /* Delete predefined shaders */
  VEShaderDeinitInternal();

  /* Remove parameters */
  VEParametersDelete();
} /* End of 'VEGraphicsDeinit' function */

/***
 * PURPOSE: Determine time between two frames rendering
 *  RETURN: The number of milliseconds, passed since last frame rendering
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VETimeElapsed( VEVOID )
{
  static VEBOOL notFirstFrame = 0;
  VEREAL timeSinceApplicationStarted = glutGet(GLUT_ELAPSED_TIME);
  VEREAL difference = (timeSinceApplicationStarted - p_GraphicsLastFrameTime) * notFirstFrame;
  p_GraphicsLastFrameTime = timeSinceApplicationStarted;

  notFirstFrame = TRUE;
  return difference;
} /* End of 'VETimeElapsed' function */
