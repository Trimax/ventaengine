#ifndef FOG_H_INCLUDED
#define FOG_H_INCLUDED

#include "physics.h"

/***
 * PURPOSE: Set fog density
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] sceneID - scene identifier to render
 *   PARAM: [IN] density - new fog density
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEFogSetDensity( const VEUINT sceneID, const VEREAL density );

/***
 * PURPOSE: Set fog color
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] sceneID - scene identifier to render
 *   PARAM: [IN] color   - new fog color
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEFogSetColor( const VEUINT sceneID, const VECOLOR color );

#endif // FOG_H_INCLUDED
