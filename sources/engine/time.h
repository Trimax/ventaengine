#ifndef TIME_H_INCLUDED
#define TIME_H_INCLUDED

#include "types.h"

/***
 * PURPOSE: Pause current thread for some time
 *   PARAM: [IN] milliseconds - pause size
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWait( const VEULONG milliseconds );

/***
 * PURPOSE: Determine number of seconds since 1970
 *  RETURN: Number of seconds since 1970
 *  AUTHOR: Eliseev Dmitry
 ***/
VEULONG VESecondsSince1970( VEVOID );

/***
 * PURPOSE: Determine number of milliseconds since application started
 *  RETURN: Number of milliseconds since application started
 *  AUTHOR: Eliseev Dmitry
 ***/
VEULONG VETime( VEVOID );

#endif // TIME_H_INCLUDED
