#ifndef LOGGER_H_INCLUDED
#define LOGGER_H_INCLUDED

#include "types.h"

/*** Definitions ***/

/* Message level: Error */
#define VE_LOGLEVEL_ERROR   0

/* Message level: Warning */
#define VE_LOGLEVEL_WARNING 1

/* Message level: Information */
#define VE_LOGLEVEL_INFO    2

/***
 * PURPOSE: Add record to log file
 *   PARAM: [IN] messageType - one of VE_LOGLEVEL_XXX definitions
 *   PARAM: [IN] message     - message to add to log file
 *   PARAM: [IN] file        - source file where message was made
 *   PARAM: [IN] line        - line number
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELoggerAdd( const VEBYTE messageLevel, const VEBUFFER message, const VEBUFFER file, const VEUINT line );

/***
 * PURPOSE: Add information record to log file
 *   PARAM: [IN] message - message to add to log file
 *  AUTHOR: Eliseev Dmitry
 ***/
#define VELoggerInfo(message)    VELoggerAdd(VE_LOGLEVEL_INFO,    message, __FILE__, __LINE__)

/***
 * PURPOSE: Add warning record to log file
 *   PARAM: [IN] message - message to add to log file
 *  AUTHOR: Eliseev Dmitry
 ***/
#define VELoggerWarning(message) VELoggerAdd(VE_LOGLEVEL_WARNING, message, __FILE__, __LINE__)

/***
 * PURPOSE: Add error record to log file
 *   PARAM: [IN] message - message to add to log file
 *  AUTHOR: Eliseev Dmitry
 ***/
#define VELoggerError(message)   VELoggerAdd(VE_LOGLEVEL_ERROR,   message, __FILE__, __LINE__)

#endif // LOGGER_H_INCLUDED
