#ifndef INTERNALBUFFER_H_INCLUDED
#define INTERNALBUFFER_H_INCLUDED

#include "internalarray.h"
#include "physics.h"
#include "types.h"

#include <stdio.h>

/***
 * PURPOSE: Determine message length
 *  RETURN: Message length
 *   PARAM: [IN] message - message to determine length
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEBufferLength( const VEBUFFER message );

/***
 * PURPOSE: Trim symbols from the begining and ending of string
 *   PARAM: [IN] message - message to trim
 *   PARAM: [IN] symbol  - symbol to trim
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBufferTrim( VEBUFFER message, const VECHAR symbol );

/***
 * PURPOSE: Remove symbols from the string
 *   PARAM: [IN] message - message to trim
 *   PARAM: [IN] symbol  - symbol to trim
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBufferRemoveAll( VEBUFFER message, const VECHAR symbol );

/***
 * PURPOSE: Replace all symbol occurrences with another one
 *   PARAM: [IN] message   - message to process
 *   PARAM: [IN] symbol    - symbol to replace
 *   PARAM: [IN] newSymbol - new symbol
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBufferReplaceAll( VEBUFFER message, const VECHAR symbol, const VECHAR newSymbol );

/***
 * PURPOSE: Compare two buffers
 *  RETURN: Difference between two strings
 *   PARAM: [IN] src1 - first string
 *   PARAM: [IN] src2 - second string
 *  AUTHOR: Eliseev Dmitry
 ***/
VEINT VEBuffersDiff( const VEBUFFER src1, const VEBUFFER src2 );

/***
 * PURPOSE: Compare two buffers
 *  RETURN: TRUE if buffers are the same, FALSE otherwise
 *   PARAM: [IN] src1 - first string
 *   PARAM: [IN] src2 - second string
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEBuffersEqual( const VEBUFFER src1, const VEBUFFER src2 );

/***
 * PURPOSE: Split buffer using separator
 *  RETURN: Pointer to splitted buffer as instance of an array of strings if success, NULL otherwise
 *   PARAM: [IN] src       - source buffer to split
 *   PARAM: [IN] separator - character, that separates values
 *  AUTHOR: Eliseev Dmitry
 ***/
VEARRAY *VEBufferSplit( const VEBUFFER src, const VECHAR separator );

/***
 * PURPOSE: Compare end of buffer with sample
 *  RETURN: TRUE if end of buffer is the same as sample, FALSE otherwise
 *   PARAM: [IN] src    - source buffer to check
 *   PARAM: [IN] sample - buffer to look up
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEBufferIsEndsWith( const VEBUFFER src, const VEBUFFER sample );

/***
 * PURPOSE: Find position of last occurrence of symbol at string
 *  RETURN: Last symbol's occurrence position if success, -1 otherwise
 *   PARAM: [IN] src    - source buffer to check
 *   PARAM: [IN] symbol - symbol to find position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEINT VEBufferLastIndexOf( const VEBUFFER src, const VECHAR symbol );

/***
 * PURPOSE: Find position of first occurrence of symbol at string starting from position
 *  RETURN: First symbol's occurrence position if success, -1 otherwise
 *   PARAM: [IN] src    - source buffer to check
 *   PARAM: [IN] from   - startup position
 *   PARAM: [IN] symbol - symbol to find position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEINT VEBufferIndexOf( const VEBUFFER src, const VEUINT from, const VECHAR symbol );

/***
 * PURPOSE: Find position of first occurrence of symbol at string
 *  RETURN: First symbol's occurrence position if success, -1 otherwise
 *   PARAM: [IN] src    - source buffer to check
 *   PARAM: [IN] symbol - symbol to find position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEINT VEBufferFirstIndexOf( const VEBUFFER src, const VECHAR symbol );

/***
 * PURPOSE: Convert buffer to an integer
 *  RETURN: Buffer's integer representation if success, 0 otherwise
 *   PARAM: [IN] src - source buffer to convert
 *  AUTHOR: Eliseev Dmitry
 ***/
VEINT VEBufferToInt( const VEBUFFER src );

/***
 * PURPOSE: Convert buffer to a real
 *  RETURN: Buffer's real representation if success, 0 otherwise
 *   PARAM: [IN] src - source buffer to convert
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEBufferToReal( const VEBUFFER src );

/***
 * PURPOSE: Convert buffer to a color
 *  RETURN: Buffer's color representation if success, VECOLOR_BLACK otherwise
 *   PARAM: [IN] src - source buffer to convert
 *  AUTHOR: Eliseev Dmitry
 ***/
VECOLOR VEBufferToColor( const VEBUFFER src );

/***
 * PURPOSE: Write buffer to output stream
 *   PARAM: [IN] out - opened output stream
 *   PARAM: [IN] buf - buffer to write
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBufferWrite( FILE *out, const VEBUFFER buf );

/***
 * PURPOSE: Read buffer from input stream
 *  RETURN: Readed buffer if success, NULL otherwise
 *   PARAM: [IN] in - opened input stream
 * COMMENT: Need to be deleted using Delete function
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBUFFER VEBufferRead( FILE *in );

#endif // INTERNALBUFFER_H_INCLUDED
