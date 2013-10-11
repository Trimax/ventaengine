#include "editor.h"

#include <string.h>
#include <stdio.h>

/* Previous mouse position */
VEINT mousePrevX = 0;
VEINT mousePrevY = 0;

/***
 * PURPOSE: Set camera position to interface label
 *   PARAM: [IN] cameraPosition - current camera position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID EditorPrintCameraPosition( VEVECTOR3D cameraPosition )
{
  VECHAR buffer[VE_BUFFER_SMALL];
  memset(buffer, 0, VE_BUFFER_SMALL);
  sprintf(buffer, "[%.2lf,%.2lf,%.2lf]", cameraPosition.m_X, cameraPosition.m_Y, cameraPosition.m_Z);
  VEInterfaceWidgetSetProperty(interfaceID, wndInformation, "lblCameraPosition", "Title", buffer);
} /* End of 'EditorPrintCameraPosition' function */

/***
 * PURPOSE: Map editor keyboard processor
 *   PARAM: [IN] key - pressed key
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID EditorKeyboardProcessor( VEKEY key )
{
  /*** Common keys ***/

  /* Stop signal */
  if (key.m_Code == VE_KEY_ESCAPE)
    VEStop();

  /*** Camera moving ***/
  switch(key.m_Code)
  {
    case 'w':
    case 'W':
      VECameraMove(sceneID, cameraID, 0.1 + 0.9 * key.m_Shift);
      EditorPrintCameraPosition(VECameraGetPosition(sceneID, cameraID));
      return;
    case 's':
    case 'S':
      VECameraMove(sceneID, cameraID, -0.1 - 0.9 * key.m_Shift);
      EditorPrintCameraPosition(VECameraGetPosition(sceneID, cameraID));
      return;
    case 'a':
    case 'A':
      VECameraStrafe(sceneID, cameraID, -0.1 - 0.9 * key.m_Shift);
      EditorPrintCameraPosition(VECameraGetPosition(sceneID, cameraID));
      return;
    case 'd':
    case 'D':
      VECameraStrafe(sceneID, cameraID, 0.1 + 0.9 * key.m_Shift);
      EditorPrintCameraPosition(VECameraGetPosition(sceneID, cameraID));
      return;
  }
} /* End of 'EditorKeyboardProcessor' function */

/***
 * PURPOSE: Map editor mouse processor
 *   PARAM: [IN] event - mouse event
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID EditorMouseProcessor( VEMOUSE event )
{
  VEINT diffX = event.m_X - mousePrevX;
  VEINT diffY = event.m_Y - mousePrevY;

  /* Strafe camera */
  if (event.m_Right)
  {
    VECameraRotateYaw(sceneID, cameraID, diffX);
    VECameraRotatePitch(sceneID, cameraID, -diffY);
  }

  /* Store new mouse position */
  mousePrevX = event.m_X;
  mousePrevY = event.m_Y;
} /* End of 'EditorMouseProcessor' function */
