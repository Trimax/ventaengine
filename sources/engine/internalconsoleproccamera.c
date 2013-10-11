#include "internalscenemanager.h"
#include "internalsettings.h"
#include "internalconsole.h"
#include "internalbuffer.h"
#include "internalstring.h"
#include "scenemanager.h"
#include "console.h"
#include "camera.h"

#include <string.h>
#include <stdio.h>

/*** Camera command arguments ***/

/* Print all cameras */
#define VE_CONSOLE_COMMANDCAMERA_LIST   "list"

/* Create a new scene */
#define VE_CONSOLE_COMMANDCAMERA_CREATE "create"

/* Delete a camera */
#define VE_CONSOLE_COMMANDCAMERA_DELETE "delete"

/* Select a camera */
#define VE_CONSOLE_COMMANDCAMERA_SELECT "select"

/***
 * PURPOSE: Process camera command
 *  RETURN: TRUE command processed, FALSE otherwise
 *   PARAM: [IN] cmdParams - command arguments
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleProcessorCamera( const VEARRAY *cmdParams )
{
  VECHAR result[VE_BUFFER_SMALL];
  VESTRING *parameter = NULL;
  if (cmdParams->m_Size <= 1)
  {
    VEConsolePrint("  not enough parameters. Type '" VE_CONSOLE_CAMERA " " VE_CONSOLE_COMMANDHELP "' to see help");
    return TRUE;
  }

  /* Switch command */
  memset(result, 0, VE_BUFFER_SMALL);
  parameter = (VESTRING*)cmdParams->m_Items[1];
  if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDCAMERA_CREATE))
  {
    VEUINT cameraID = VECameraCreate(p_SettingsGraphicsSceneID);

    sprintf(result, "  camera created. ID: %d", cameraID);
    VEConsolePrint(result);

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDCAMERA_DELETE))
  {
    VESTRING *cameraID = (VESTRING*)cmdParams->m_Items[2];
    if (cmdParams->m_Size <= 2)
    {
      VEConsolePrint("  not enough parameters. Use " VE_CONSOLE_CAMERA " " VE_CONSOLE_COMMANDCAMERA_DELETE " <camera identifier>");
      return TRUE;
    }

    VECameraDelete(p_SettingsGraphicsSceneID, VEBufferToInt(cameraID->m_Data));
    VEConsolePrintArg("  deleted camera: ", cameraID->m_Data);

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDCAMERA_SELECT))
  {
    VESTRING *cameraID = (VESTRING*)cmdParams->m_Items[2];
    if (cmdParams->m_Size <= 2)
    {
      VEConsolePrint("  not enough parameters. Use " VE_CONSOLE_CAMERA " " VE_CONSOLE_COMMANDCAMERA_SELECT " <camera identifier>");
      return TRUE;
    }

    if (VECameraSelect(p_SettingsGraphicsSceneID, VEBufferToInt(cameraID->m_Data)))
      VEConsolePrintArg("  selected camera: ", cameraID->m_Data);
    else
      VEConsolePrintArg("  camera doesn't exist: ", cameraID->m_Data);

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDCAMERA_LIST))
  {
    VECameraList();
    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDHELP))
  {
    VEConsolePrint("Command '"VE_CONSOLE_CAMERA"'");
    VEConsolePrint("  " VE_CONSOLE_CAMERA " " VE_CONSOLE_COMMANDHELP          " - show help screen");
    VEConsolePrint("  " VE_CONSOLE_CAMERA " " VE_CONSOLE_COMMANDCAMERA_LIST   " - print current scene cameras list");
    VEConsolePrint("  " VE_CONSOLE_CAMERA " " VE_CONSOLE_COMMANDCAMERA_CREATE " - create camera");
    VEConsolePrint("  " VE_CONSOLE_CAMERA " " VE_CONSOLE_COMMANDCAMERA_SELECT " - select camera");
    VEConsolePrint("  " VE_CONSOLE_CAMERA " " VE_CONSOLE_COMMANDCAMERA_DELETE " - delete camera");

    return TRUE;
  }

  /* That's it */
  return FALSE;
} /* End of 'VEConsoleProcessorCamera' function */
