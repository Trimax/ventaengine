#include "internalscenemanager.h"
#include "internalsettings.h"
#include "internalconsole.h"
#include "internalbuffer.h"
#include "internalstring.h"
#include "internallight.h"
#include "scenemanager.h"
#include "console.h"
#include "light.h"

#include <string.h>
#include <stdio.h>

/*** Camera command arguments ***/

/* Print all light sources */
#define VE_CONSOLE_COMMANDLIGHT_LIST   "list"

/* Create a new light */
#define VE_CONSOLE_COMMANDLIGHT_CREATE "create"

/* Delete a light */
#define VE_CONSOLE_COMMANDLIGHT_DELETE "delete"

/***
 * PURPOSE: Process light command
 *  RETURN: TRUE command processed, FALSE otherwise
 *   PARAM: [IN] cmdParams - command arguments
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleProcessorLight( const VEARRAY *cmdParams )
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
  if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDLIGHT_CREATE))
  {
    VEUINT lightID = VELightCreate(p_SettingsGraphicsSceneID);

    sprintf(result, "  light created. ID: %d", lightID);
    VEConsolePrint(result);

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDLIGHT_DELETE))
  {
    VESTRING *lightID = (VESTRING*)cmdParams->m_Items[2];
    if (cmdParams->m_Size <= 2)
    {
      VEConsolePrint("  not enough parameters. Use " VE_CONSOLE_LIGHT " " VE_CONSOLE_COMMANDLIGHT_DELETE " <light identifier>");
      return TRUE;
    }

    VELightDelete(p_SettingsGraphicsSceneID, VEBufferToInt(lightID->m_Data));
    VEConsolePrintArg("  deleted light: ", lightID->m_Data);

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDLIGHT_LIST))
  {
    VELightList();
    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDHELP))
  {
    VEConsolePrint("Command '"VE_CONSOLE_LIGHT"'");
    VEConsolePrint("  " VE_CONSOLE_LIGHT " " VE_CONSOLE_COMMANDHELP         " - show help screen");
    VEConsolePrint("  " VE_CONSOLE_LIGHT " " VE_CONSOLE_COMMANDLIGHT_LIST   " - print current scene light sources list");
    VEConsolePrint("  " VE_CONSOLE_LIGHT " " VE_CONSOLE_COMMANDLIGHT_CREATE " - create light");
    VEConsolePrint("  " VE_CONSOLE_LIGHT " " VE_CONSOLE_COMMANDLIGHT_DELETE " - delete lights");

    return TRUE;
  }

  /* That's it */
  return FALSE;
} /* End of 'VEConsoleProcessorLight' function */
