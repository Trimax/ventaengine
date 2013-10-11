#include "internalscenemanager.h"
#include "internalsettings.h"
#include "internalconsole.h"
#include "internalbuffer.h"
#include "internalstring.h"
#include "scenemanager.h"
#include "console.h"
#include "object.h"

#include <string.h>
#include <stdio.h>

/*** Object command arguments ***/

/* Print all scenes */
#define VE_CONSOLE_COMMANDOBJECT_LIST   "list"

/* Create a new scene */
#define VE_CONSOLE_COMMANDOBJECT_LOAD   "load"

/* Delete a scene */
#define VE_CONSOLE_COMMANDOBJECT_DELETE "delete"

/***
 * PURPOSE: Process object command
 *  RETURN: TRUE command processed, FALSE otherwise
 *   PARAM: [IN] cmdParams - command arguments
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleProcessorObject( const VEARRAY *cmdParams )
{
  VECHAR result[VE_BUFFER_SMALL];
  VESTRING *parameter = NULL;
  if (cmdParams->m_Size <= 1)
  {
    VEConsolePrint("  not enough parameters. Type '" VE_CONSOLE_OBJECT " " VE_CONSOLE_COMMANDHELP "' to see help");
    return TRUE;
  }

  /* Switch command */
  memset(result, 0, VE_BUFFER_SMALL);
  parameter = (VESTRING*)cmdParams->m_Items[1];
  if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDOBJECT_LOAD))
  {
    VEUINT objectID = 0;
    VESTRING *objectName = (VESTRING*)cmdParams->m_Items[2];
    if (cmdParams->m_Size <= 2)
    {
      VEConsolePrint("  not enough parameters. Use " VE_CONSOLE_OBJECT " " VE_CONSOLE_COMMANDOBJECT_LOAD " <object file name>");
      return TRUE;
    }
    objectID = VEObjectLoad(p_SettingsGraphicsSceneID, objectName->m_Data);

    sprintf(result, "  object loaded. ID: %d", objectID);
    VEConsolePrint(result);

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDOBJECT_DELETE))
  {
    VESTRING *objectID = (VESTRING*)cmdParams->m_Items[2];
    if (cmdParams->m_Size <= 2)
    {
      VEConsolePrint("  not enough parameters. Use " VE_CONSOLE_OBJECT " " VE_CONSOLE_COMMANDOBJECT_DELETE " <object identifier>");
      return TRUE;
    }

    VEObjectDelete(p_SettingsGraphicsSceneID, VEBufferToInt(objectID->m_Data));
    VEConsolePrintArg("  deleted object: ", objectID->m_Data);

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDOBJECT_LIST))
  {
    VEObjectList();
    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDHELP))
  {
    VEConsolePrint("Command '"VE_CONSOLE_OBJECT"'");
    VEConsolePrint("  " VE_CONSOLE_OBJECT " " VE_CONSOLE_COMMANDHELP          " - show help screen");
    VEConsolePrint("  " VE_CONSOLE_OBJECT " " VE_CONSOLE_COMMANDOBJECT_LIST   " - print current scene objects list");
    VEConsolePrint("  " VE_CONSOLE_OBJECT " " VE_CONSOLE_COMMANDOBJECT_LOAD   " - load object from file");
    VEConsolePrint("  " VE_CONSOLE_OBJECT " " VE_CONSOLE_COMMANDOBJECT_DELETE " - delete object");

    return TRUE;
  }

  /* That's it */
  return FALSE;
} /* End of 'VEConsoleProcessorObject' function */
