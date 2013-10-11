#ifndef STRING_H_INCLUDED
#define STRING_H_INCLUDED

#include "types.h"

/***
 * PURPOSE: Create empty string
 *  RETURN: Created string identifier if success, 0 otherwise
 *   PARAM: [IN] size - string length
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStringCreate( const VEUINT size );

/***
 * PURPOSE: Create string using C-style buffer
 *  RETURN: Created string identifier if success, 0 otherwise
 *   PARAM: [IN] sourceString - C-style string representation
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStringCreateFromString( const VEBUFFER sourceString );

/***
 * PURPOSE: Create a copy of string
 *  RETURN: Copied string identifier if success, 0 otherwise
 *   PARAM: [IN] stringID - source string identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStringCopy( const VEUINT stringID );

/***
 * PURPOSE: Delete existing string
 *   PARAM: [IN] stringID - identifier of string to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStringDelete( const VEUINT stringID );

/***
 * PURPOSE: Obtain C-style pointer to data buffer
 *  RETURN: Pointer to data buffer if success, NULL otherwise
 *   PARAM: [IN] stringID - string identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
const VEBUFFER VEStringPtr( const VEUINT stringID );

/***
 * PURPOSE: Determine string length
 *  RETURN: Length of string
 *   PARAM: [IN] stringID - string identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStringLength( const VEUINT stringID );

/***
 * PURPOSE: Add two strings
 *  RETURN: Identifier of newly created string, which is concatenation of the others if success, 0 otherwise
 *   PARAM: [IN] leftStringID  - first string identifier
 *   PARAM: [IN] rightStringID - second string identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStringAdd( const VEUINT leftStringID, const VEUINT rightStringID );

/***
 * PURPOSE: Erase string
 *   PARAM: [IN] stringID - string identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStringClear( const VEUINT stringID );

#endif // STRING_H_INCLUDED
