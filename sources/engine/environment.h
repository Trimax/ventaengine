#ifndef ENVIRONMENT_H_INCLUDED
#define ENVIRONMENT_H_INCLUDED

#include "types.h"
#include "color.h"

/*** Default sun velocity ***/
#define VE_SUNVELOCITY_DEFAULT 1.0

/*** Default sun deviation ***/
#define VE_SUNDEVIATION_DEFAULT 0.0

/***
 * PURPOSE: Load environment into scene
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] sceneID      - scene identifier to render
 *   PARAM: [IN] sunVelocity  - default sun velocity
 *   PARAM: [IN] sunDeviation - default sun deviation from (XZ) plane
 *   PARAM: [IN] dayColor     - skyColor
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEEnvironmentCreate( const VEUINT sceneID, const VEREAL sunVelocity, const VEREAL sunDeviation, const VECOLOR skyColor );

/***
 * PURPOSE: Unload environment from scene
 *   PARAM: [IN] sceneID - scene identifier to render
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEEnvironmentDelete( const VEUINT sceneID );

#endif // ENVIRONMENT_H_INCLUDED
