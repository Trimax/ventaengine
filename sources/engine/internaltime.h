#ifndef INTERNALTIME_H_INCLUDED
#define INTERNALTIME_H_INCLUDED

#include "types.h"

/***
 * PURPOSE: Determine time between two frames rendering
 *  RETURN: The number of milliseconds, passed since last frame rendering
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VETimeElapsed( VEVOID );

#endif // INTERNALTIME_H_INCLUDED
