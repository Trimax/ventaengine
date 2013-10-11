#include "internalconsole.h"
#include "internalstring.h"
#include "internalbuffer.h"
#include "internalbuffer.h"
#include "internalcore.h"
#include "console.h"
#include "engine.h"
#include "input.h"

#include <stdlib.h>
#include <string.h>

/* Array identifier of console command */
#define VE_CONSOLE_CMDID 0

/***
 * PURPOSE: Execute command
 *  RETURN: TRUE if command was successfull, FALSE otherwise
 *   PARAM: [IN] cmdParams - command and parameters
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleExecuteInternal( const VEARRAY *cmdParams )
{
  VESTRING *command = cmdParams->m_Items[VE_CONSOLE_CMDID];

  /* Empty command (can not be, because of trimming) */
  if (!command)
    return FALSE;

  /* Switch command */
  if ((VEBuffersEqual(command->m_Data, VE_CONSOLE_COMMANDEXIT))||(VEBuffersEqual(command->m_Data, VE_CONSOLE_COMMANDQUIT)))
  {
    VEConsolePrint("  stop signal sent");
    VEStop();
    return TRUE;
  } else if ((VEBuffersEqual(command->m_Data, VE_CONSOLE_COMMANDVER))||(VEBuffersEqual(command->m_Data, VE_CONSOLE_COMMANDVERSION)))
  {
    VEConsolePrint(VE_VERSION);
    return TRUE;
  } else if (VEBuffersEqual(command->m_Data, VE_CONSOLE_COMMANDHELP))
  {
    VEConsolePrint("Venta engine console commands:");
    VEConsolePrint("  " VE_CONSOLE_COMMANDHELP    " - print help screen");
    VEConsolePrint("  " VE_CONSOLE_COMMANDQUIT    " - shut down engine");
    VEConsolePrint("  " VE_CONSOLE_COMMANDEXIT    " - shut down engine");
    VEConsolePrint("  " VE_CONSOLE_COMMANDVER     " - print engine version");
    VEConsolePrint("  " VE_CONSOLE_COMMANDVERSION " - print engine version");
    VEConsolePrint("  " VE_CONSOLE_RENDER         " - rendering commands block");
    VEConsolePrint("  " VE_CONSOLE_RESOURCE       " - resources commands block");
    VEConsolePrint("  " VE_CONSOLE_SCENE          " - scenes commands block");
    VEConsolePrint("  " VE_CONSOLE_OBJECT         " - objects commands block");
    VEConsolePrint("  " VE_CONSOLE_CAMERA         " - cameras commands block");
    VEConsolePrint("  " VE_CONSOLE_LIGHT          " - lights commands block");

    return TRUE;
  } else if (VEBuffersEqual(command->m_Data, VE_CONSOLE_RENDER))
    return VEConsoleProcessorRender(cmdParams);
  else if (VEBuffersEqual(command->m_Data, VE_CONSOLE_SCENE))
    return VEConsoleProcessorScene(cmdParams);
  else if (VEBuffersEqual(command->m_Data, VE_CONSOLE_OBJECT))
    return VEConsoleProcessorObject(cmdParams);
  else if (VEBuffersEqual(command->m_Data, VE_CONSOLE_RESOURCE))
    return VEConsoleProcessorResource(cmdParams);
  else if (VEBuffersEqual(command->m_Data, VE_CONSOLE_CAMERA))
    return VEConsoleProcessorCamera(cmdParams);
  else if (VEBuffersEqual(command->m_Data, VE_CONSOLE_LIGHT))
    return VEConsoleProcessorLight(cmdParams);

  /* Command wasn't processed */
  return FALSE;
} /* End of 'VEConsoleExecuteInternal' function */

/***
 * PURPOSE: Execute command
 *  RETURN: TRUE if command was successfull, FALSE otherwise
 *   PARAM: [IN] command - command to process
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleProcess( const VEBUFFER command )
{
  VEBOOL result = TRUE;
  VEARRAY *arguments = NULL;
  VEBufferTrim(command, VE_KEY_SPACE);

  /* Not process commands if engine in stopping mode */
  if (VEIsStopping())
    return FALSE;

  /* Split command by spacers */
  arguments = VEBufferSplit(command, VE_KEY_SPACE);
  if (!arguments)
    return FALSE;

  /* Execute command */
  result = VEConsoleExecuteInternal(arguments);
  if (!result)
    result = VEConsoleExecuteProcessorInternal(command);

  /* Release memory */
  VEArrayForeachInternal(arguments, (VEFUNCTION)VEStringDeleteInternal);
  VEArrayDeleteInternal(arguments);

  /* That's it */
  return result;
} /* End of 'VEConsoleProcess' function */
