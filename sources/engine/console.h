#ifndef CONSOLE_H_INCLUDED
#define CONSOLE_H_INCLUDED

#include "physics.h"
#include "types.h"
#include "math.h"

/***
 * PURPOSE: Print a message to console
 *   PARAM: [IN] message - message to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrint( const VEBUFFER message );

/***
 * PURPOSE: Print an integer to a console
 *   PARAM: [IN] message - message to print
 *   PARAM: [IN] number  - integer number to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrintUINT( const VEBUFFER message, const VEUINT number );

/***
 * PURPOSE: Print a real to a console
 *   PARAM: [IN] message - message to print
 *   PARAM: [IN] number  - real number to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrintREAL( const VEBUFFER message, const VEREAL number );

/***
 * PURPOSE: Print a 2D vector to a console
 *   PARAM: [IN] message - message to print
 *   PARAM: [IN] vec     - 2D vector to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrintVECTOR2D( const VEBUFFER message, const VEVECTOR2D vec );

/***
 * PURPOSE: Print a 3D vector to a console
 *   PARAM: [IN] message - message to print
 *   PARAM: [IN] vec     - 3D vector to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrintVECTOR3D( const VEBUFFER message, const VEVECTOR3D vec );

/***
 * PURPOSE: Print a 4D vector to a console
 *   PARAM: [IN] message - message to print
 *   PARAM: [IN] vec     - 4D vector to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrintVECTOR4D( const VEBUFFER message, const VEVECTOR4D vec );

/***
 * PURPOSE: Print a color to a console
 *   PARAM: [IN] message - message to print
 *   PARAM: [IN] color   - color to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrintCOLOR( const VEBUFFER message, const VECOLOR color );

/***
 * PURPOSE: Print a message with argument to console
 *   PARAM: [IN] message - message to print
 *   PARAM: [IN] arg     - message's argument
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrintArg( const VEBUFFER message, const VEBUFFER arg );

/***
 * PURPOSE: Execute command
 *  RETURN: TRUE if command was successfull, FALSE otherwise
 *   PARAM: [IN] command - command to process
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleProcess( const VEBUFFER command );

#endif // CONSOLE_H_INCLUDED
