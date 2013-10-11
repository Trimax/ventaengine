#ifndef FS_H_INCLUDED
#define FS_H_INCLUDED

#include "types.h"

/***
 * PURPOSE: Open binary file to read
 *  RETURN: Pointer to opened file if success, 0 otherwise
 *   PARAM: [IN] fileName - file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEFSOpen( const VEBUFFER fileName );

/***
 * PURPOSE: Open binary file to write
 *  RETURN: Pointer to created file if success, 0 otherwise
 *   PARAM: [IN] fileName - file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEFSCreate( const VEBUFFER fileName );

/***
 * PURPOSE: Close opened binary file
 *   PARAM: [IN] id - file identfier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEFSClose( const VEUINT id );

#endif // FS_H_INCLUDED
