#ifndef POOL_H_INCLUDED
#define POOL_H_INCLUDED

#include "types.h"

/***
 * PURPOSE: Create new pool
 *  RETURN: Newly created thread identifier if success, 0 otherwise
 *   PARAM: [IN] size - pool size
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEPoolCreate( const VEUINT size );

/***
 * PURPOSE: Delete existing pool
 *   PARAM: [IN] poolID - existing pool identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEPoolDelete( const VEUINT poolID );

/***
 * PURPOSE: Add new task to pool
 *   PARAM: [IN] poolID   - existing pool identifier
 *   PARAM: [IN] function - function, that will be called from new thread
 *   PARAM: [IN] data     - user's data, that will be passed to function
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEPoolPush( const VEUINT poolID, const VEFUNCTION function, const VEPOINTER data );

#endif // POOL_H_INCLUDED
