#include "internalscenemanager.h"
#include "internalsettings.h"
#include "internalconsole.h"
#include "internalbuffer.h"
#include "internalstring.h"
#include "scenemanager.h"
#include "console.h"

#include <string.h>
#include <stdio.h>

/*** Scene command arguments ***/

/* Print all scenes */
#define VE_CONSOLE_COMMANDSCENE_LIST   "list"

/* Create a new scene */
#define VE_CONSOLE_COMMANDSCENE_CREATE "create"

/* Delete a scene */
#define VE_CONSOLE_COMMANDSCENE_DELETE "delete"

/* Select scene by ID */
#define VE_CONSOLE_COMMANDSCENE_SELECT "select"

/***
 * PURPOSE: Process render command
 *  RETURN: TRUE command processed, FALSE otherwise
 *   PARAM: [IN] cmdParams - command arguments
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleProcessorScene( const VEARRAY *cmdParams )
{
  VECHAR result[VE_BUFFER_SMALL];
  VESTRING *parameter = NULL;
  if (cmdParams->m_Size <= 1)
  {
    VEConsolePrint("  not enough parameters. Type '" VE_CONSOLE_SCENE " " VE_CONSOLE_COMMANDHELP "' to see help");
    return TRUE;
  }

  /* Switch command */
  memset(result, 0, VE_BUFFER_SMALL);
  parameter = (VESTRING*)cmdParams->m_Items[1];
  if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDSCENE_CREATE))
  {
    VEUINT sceneID = VESceneCreate();
    sprintf(result, "  scene created. ID: %d", sceneID);
    VEConsolePrint(result);

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDSCENE_DELETE))
  {
    VESTRING *sceneID = (VESTRING*)cmdParams->m_Items[2];
    if (cmdParams->m_Size <= 2)
    {
      VEConsolePrint("  not enough parameters. Use " VE_CONSOLE_SCENE " " VE_CONSOLE_COMMANDSCENE_DELETE " <scene identifier>");
      return TRUE;
    }

    VESceneDelete(VEBufferToInt(sceneID->m_Data));
    VEConsolePrintArg("  deleted scene: ", sceneID->m_Data);

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDSCENE_SELECT))
  {
    VESTRING *sceneID = (VESTRING*)cmdParams->m_Items[2];
    if (cmdParams->m_Size <= 2)
    {
      VEConsolePrint("  not enough parameters. Use " VE_CONSOLE_SCENE " " VE_CONSOLE_COMMANDSCENE_SELECT " <scene identifier>");
      return TRUE;
    }

    VESceneSelect(VEBufferToInt(sceneID->m_Data));
    VEConsolePrintArg("  selected scene: ", sceneID->m_Data);

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDSCENE_LIST))
  {
    VESceneList();
    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDHELP))
  {
    VEConsolePrint("Command '"VE_CONSOLE_SCENE"'");
    VEConsolePrint("  " VE_CONSOLE_SCENE " " VE_CONSOLE_COMMANDHELP         " - show help screen");
    VEConsolePrint("  " VE_CONSOLE_SCENE " " VE_CONSOLE_COMMANDSCENE_LIST   " - print scenes list");
    VEConsolePrint("  " VE_CONSOLE_SCENE " " VE_CONSOLE_COMMANDSCENE_CREATE " - create new scene");
    VEConsolePrint("  " VE_CONSOLE_SCENE " " VE_CONSOLE_COMMANDSCENE_SELECT " - select scene by id");

    return TRUE;
  }

  /* That's it */
  return FALSE;
} /* End of 'VEConsoleProcessorScene' function */
