#ifndef INTERNALSTRING_H_INCLUDED
#define INTERNALSTRING_H_INCLUDED

#include "types.h"

/* String structure */
typedef struct tagVESTRING
{
  VEUINT   m_Size;   /* String size (allocated bytes) */
  VEUINT   m_Length; /* String length (used by data) */

  VEBUFFER m_Data;   /* String data (buffer) */
} VESTRING;

/***
 * PURPOSE: Initialize strings manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEStringInit( VEVOID );

/***
 * PURPOSE: Create empty string
 *  RETURN: Pointer to created string if success, NULL otherwise
 *   PARAM: [IN] size - string length
 *  AUTHOR: Eliseev Dmitry
 ***/
VESTRING* VEStringCreateInternal( const VEUINT size );

/***
 * PURPOSE: Create string from buffer
 *  RETURN: Pointer to created string if success, NULL otherwise
 *   PARAM: [IN] buffer - buffer
 *  AUTHOR: Eliseev Dmitry
 ***/
VESTRING* VEStringCreateFromBufferInternal( const VEBUFFER buffer );

/***
 * PURPOSE: Delete existing string
 *   PARAM: [IN] stringPtr - pointer to string structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStringDeleteInternal( VESTRING *stringPtr );

/***
 * PURPOSE: Deinitialize strings manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStringDeinit( VEVOID );

#endif // INTERNALSTRING_H_INCLUDED
