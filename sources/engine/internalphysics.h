#ifndef INTERNALPHYSICS_H_INCLUDED
#define INTERNALPHYSICSN_H_INCLUDED

#include "types.h"

/* Time between two frames */
#define VE_FRAME_DURATION 30.0

/* Minimal gravity distance */
#define VE_MIN_GRAVITY_DISTANCE 0.5

/* One frame dt */
#define VE_FRAME_DT 0.08

/***
 * PURPOSE: Update objects position according to gravitation
 *   PARAM: [IN] timeElapsed - time elapsed since last frame rendering (milliseconds)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEApplyGravity( const VEREAL timeElapsed );

#endif // CONFIGURATION_H_INCLUDED
