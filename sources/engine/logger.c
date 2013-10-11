#include "internalcriticalsection.h"
#include "internallogger.h"
#include "internalbuffer.h"
#include "memorymanager.h"
#include "console.h"
#include "logger.h"

#include <string.h>
#include <stdio.h>
#include <time.h>

/* Logger structure */
typedef struct tagVELOGGER
{
  FILE              *m_File;     /* Output file */
  VEBYTE             m_Level;    /* Reporting level */
  VEUINT             m_Errors;   /* Number of errors */
  VEUINT             m_Warnings; /* Number of warnings */
  VEUINT             m_Infos;    /* Number of information messages */
  VEBOOL             m_Console;  /* Duplicate messages to console flag */

  VECRITICALSECTION *m_Sync;     /* Logger critical section */
} VELOGGER;

/* Logger instance */
volatile static VELOGGER *p_Logger = NULL;

/*** Definitions ***/

/* Error constant */
#define VE_LOGERROR   "  ERROR"

/* Warning constant */
#define VE_LOGWARNING "WARNING"

/* Information constant */
#define VE_LOGINFO    "   INFO"

/***
 * PURPOSE: Initialize logger
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] logLevel           - logging level threshold
 *   PARAM: [IN] consoleDuplication - duplicate all messages to console flag
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VELoggerInit( const VEBYTE logLevel, const VEBOOL consoleDuplication )
{
  /* Already initialized */
  if (p_Logger)
    return TRUE;

  p_Logger = New(sizeof(VELOGGER), "#Logger");
  if (!p_Logger)
    return FALSE;

  /* Store console duplication flag */
  p_Logger->m_Console = consoleDuplication;

  p_Logger->m_Sync = VESectionCreateInternal("#Logger critical section");
  if (!p_Logger->m_Sync)
  {
    Delete((VEPOINTER)p_Logger);
    p_Logger = NULL;
    return FALSE;
  }

  p_Logger->m_File = fopen(VE_LOGGER_FILE, "wt");
  if (!p_Logger->m_File)
  {
    VESectionDeleteInternal(p_Logger->m_Sync);
    Delete((VEPOINTER)p_Logger);
    p_Logger = FALSE;
    return FALSE;
  }

  p_Logger->m_Level = logLevel;
  fprintf(p_Logger->m_File, "Venta Engine Log File. Author: Eliseev Dmitry. 2011. St. Petersburg. Russia.\n\n");
  VELoggerInfo("Venta engine logger started");

  /* That's it */
  return TRUE;
} /* End of 'VELoggerInit' function */

/***
 * PURPOSE: Deinitialize logger
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELoggerDeinit( VEVOID )
{
  VECHAR resultMessage[VE_BUFFER_STANDART];
  memset(resultMessage, 0, VE_BUFFER_STANDART);

  if (!p_Logger)
    return;

  sprintf(resultMessage, "\nErrors: %d; Warnings: %d; Information: %d\n", p_Logger->m_Errors, p_Logger->m_Warnings, p_Logger->m_Infos);
  fprintf(p_Logger->m_File, "%s", resultMessage);

  if (p_Logger->m_Errors == 0)
    fprintf(p_Logger->m_File, "Venta engine sucessfully completed");

  /* Close logger file */
  fclose(p_Logger->m_File);

  /* Remove critical section */
  VESectionDeleteInternal(p_Logger->m_Sync);

  Delete((VEPOINTER)p_Logger);
  p_Logger = NULL;
} /* End of 'VELoggerDeinit' functioin */

/***
 * PURPOSE: Add record to log file
 *   PARAM: [IN] messageType - one of VE_LOGLEVEL_XXX definitions
 *   PARAM: [IN] message     - message to add to log file
 *   PARAM: [IN] file        - source file where message was made
 *   PARAM: [IN] line        - line number
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELoggerAdd( const VEBYTE messageLevel, const VEBUFFER message, const VEBUFFER file, const VEUINT line )
{
  VECHAR resultMessage[VE_BUFFER_EXTRALARGE];
  memset(resultMessage, 0, VE_BUFFER_EXTRALARGE);

  /* Logger created */
  if (p_Logger&&message)
  {
    time_t rawtime;
    struct tm *timeinfo;
    VECHAR stamp[VE_BUFFER_SMALL];
    memset(stamp, 0, VE_BUFFER_SMALL);

    if (messageLevel > p_Logger->m_Level)
      return;

    if (VEBufferLength(message) == 0)
    {
      fprintf(p_Logger->m_File, "\n");
      return;
    }

    /* Is it separator? */
    if (message[0] == '#')
    {
      fprintf(p_Logger->m_File, "--------------------------------------------------------------------------------\n");
      return;
    }

    /* Determine time */
    time (&rawtime);
    timeinfo = localtime (&rawtime);
    strftime(stamp, VE_BUFFER_SMALL, "[%Y-%m-%d %H:%M:%S]", timeinfo);

    /* Output message */
    VESectionEnterInternal(p_Logger->m_Sync);
    switch(messageLevel)
    {
    case VE_LOGLEVEL_ERROR:
      sprintf(resultMessage, "%s %s: %s at %s:%d\n", stamp, VE_LOGERROR, message, file, line);
      p_Logger->m_Errors++;
      break;
    case VE_LOGLEVEL_WARNING:
      sprintf(resultMessage, "%s %s: %s at %s:%d\n", stamp, VE_LOGWARNING, message, file, line);
      p_Logger->m_Warnings++;
      break;
    case VE_LOGLEVEL_INFO:
      sprintf(resultMessage, "%s %s: %s at %s:%d\n", stamp, VE_LOGINFO, message, file, line);
      p_Logger->m_Infos++;
      break;
    }

    /* Output message to console */
    fprintf(p_Logger->m_File, "%s", resultMessage);
    if (p_Logger->m_Console)
      VEConsolePrint(resultMessage);

    VESectionLeaveInternal(p_Logger->m_Sync);
  }
} /* End of 'VELoggerAdd' function */
