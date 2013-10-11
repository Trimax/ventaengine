#ifndef THREAD_H_INCLUDED
#define THREAD_H_INCLUDED

#include "types.h"

/***
 * PURPOSE: Create new thread
 *  RETURN: Newly created thread identifier if success, 0 otherwise
 *   PARAM: [IN] function - function, that will be called from new thread
 *   PARAM: [IN] data     - user's data, that will be passed to function
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEThreadCreate( const VEFUNCTION function, const VEPOINTER data );

/***
 * PURPOSE: Delete existing thread
 *   PARAM: [IN] threadID - existing thread identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEThreadDelete( const VEUINT threadID );

/***
 * PURPOSE: Join existing thread
 *   PARAM: [IN] threadID - existing thread identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEThreadJoin( const VEUINT threadID );

#endif // THREAD_H_INCLUDED
