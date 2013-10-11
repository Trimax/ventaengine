#ifndef MEMORYMANAGER_H_INCLUDED
#define MEMORYMANAGER_H_INCLUDED

#include "types.h"

/***
 * PURPOSE: Memory allocation
 *  RETURN: Allocated memory if success, NULL otherwise
 *   PARAM: [IN] size    - size of block to allocate (should be a positive number)
 *   PARAM: [IN] comment - comment to allocated block (can be NULL)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER New( const VEULONG size, const VEBUFFER comment );

/***
 * PURPOSE: Memory releasing
 *   PARAM: [IN] ptr - pointer to allocated memory block (returned by 'New' function)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID Delete( VEPOINTER ptr );

/***
 * PURPOSE: Get amount of used memory
 *  RETURN: Used memory amount
 *  AUTHOR: Eliseev Dmitry
 ***/
VEULONG MemoryUsed( VEVOID );

/***
 * PURPOSE: Print memory information to file
 *   PARAM: [IN] outFile - file to output memory information
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID MemoryDump( VEVOID );

#endif // MEMORYMANAGER_H_INCLUDED
