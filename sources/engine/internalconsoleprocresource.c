#include "internalresourcemanager.h"
#include "internalsettings.h"
#include "resourcemanager.h"
#include "internalconsole.h"
#include "internalbuffer.h"
#include "internalstring.h"
#include "console.h"

/*** Render command arguments ***/

/* Register resource path */
#define VE_CONSOLE_COMMANDRESOURCE_REGISTERPATH    "registerpath"

/* Register resource archive */
#define VE_CONSOLE_COMMANDRESOURCE_REGISTERARCHIVE "registerarchive"

/* Unregister resource */
#define VE_CONSOLE_COMMANDRESOURCE_UNREGISTER      "unregister"

/* Show all registered resources */
#define VE_CONSOLE_COMMANDRESOURCE_LIST            "list"

/***
 * PURPOSE: Process resource command
 *  RETURN: TRUE command processed, FALSE otherwise
 *   PARAM: [IN] cmdParams - command arguments
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleProcessorResource( const VEARRAY *cmdParams )
{
  VESTRING *parameter = NULL;
  if (cmdParams->m_Size <= 1)
  {
    VEConsolePrint("  not enough parameters. Type '" VE_CONSOLE_RESOURCE " " VE_CONSOLE_COMMANDHELP "' to see help");
    return TRUE;
  }

  parameter = (VESTRING*)cmdParams->m_Items[1];
  if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDHELP))
  {
    VEConsolePrint("Command 'resource'");
    VEConsolePrint("  " VE_CONSOLE_RESOURCE " " VE_CONSOLE_COMMANDHELP                     " - show help screen");
    VEConsolePrint("  " VE_CONSOLE_RESOURCE " " VE_CONSOLE_COMMANDRESOURCE_REGISTERPATH    " - register resources path");
    VEConsolePrint("  " VE_CONSOLE_RESOURCE " " VE_CONSOLE_COMMANDRESOURCE_REGISTERARCHIVE " - register resources archive");
    VEConsolePrint("  " VE_CONSOLE_RESOURCE " " VE_CONSOLE_COMMANDRESOURCE_UNREGISTER      " - unregister resource");

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDRESOURCE_REGISTERPATH))
  {
    VESTRING *name = NULL;
    VESTRING *path = NULL;

    if (cmdParams->m_Size <= 3)
    {
      VEConsolePrint("  not enough parameters. Use " VE_CONSOLE_RESOURCE " " VE_CONSOLE_COMMANDRESOURCE_REGISTERPATH " <resource name> <resource path>");
      return TRUE;
    }

    /* Get arguments */
    name = (VESTRING*)cmdParams->m_Items[2];
    path = (VESTRING*)cmdParams->m_Items[3];

    if (VEResourceRegisterPath(name->m_Data, path->m_Data))
      VEConsolePrintArg("  resource registered as: ", name->m_Data);
    else
      VEConsolePrintArg("  can't register path: ", path->m_Data);

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDRESOURCE_UNREGISTER))
  {
    VESTRING *name = NULL;

    if (cmdParams->m_Size <= 2)
    {
      VEConsolePrint("  not enough parameters. Use " VE_CONSOLE_RESOURCE " " VE_CONSOLE_COMMANDRESOURCE_UNREGISTER " <resource name>");
      return TRUE;
    }

    /* Get arguments */
    name = (VESTRING*)cmdParams->m_Items[2];

    /* Unregister resource */
    VEResourceUnregister(name->m_Data);
    VEConsolePrintArg("  resource unregistered: ", name->m_Data);

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDRESOURCE_LIST))
  {
    VEResourcesList();
    return TRUE;
  }

  /* That's it */
  return FALSE;
} /* End of 'VEConsoleProcessorRender' function */
