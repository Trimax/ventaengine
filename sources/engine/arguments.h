#ifndef ARGUMENTS_H_INCLUDED
#define ARGUMENTS_H_INCLUDED

#include "types.h"

/***
 * PURPOSE: Find argument in command line
 *  RETURN: TRUE if argument was defined in command-line, FALSE otherwise
 *   PARAM: [IN] name - argument name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsArgumentDefined( const VEBUFFER name );

/***
 * PURPOSE: Find argument in command line
 *  RETURN: Argument value if argument defined, NULL otherwise
 *   PARAM: [IN] name - argument name
 * COMMENT: No need to release memory after using of function
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBUFFER VEArgumentGetValue( const VEBUFFER name );

#endif // ARGUMENTS_H_INCLUDED
