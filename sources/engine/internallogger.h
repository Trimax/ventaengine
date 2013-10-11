#ifndef INTERNALLOGGER_H_INCLUDED
#define INTERNALLOGGER_H_INCLUDED

#include "types.h"

/*** Definitions ***/

/* Output file */
#define VE_LOGGER_FILE "report.txt"

/***
 * PURPOSE: Initialize logger
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] logLevel           - logging level threshold
 *   PARAM: [IN] consoleDuplication - duplicate all messages to console flag
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VELoggerInit( const VEBYTE logLevel, const VEBOOL consoleDuplication );

/***
 * PURPOSE: Deinitialize logger
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELoggerDeinit( VEVOID );

#endif // INTERNALLOGGER_H_INCLUDED
