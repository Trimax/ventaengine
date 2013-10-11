#ifndef BILLBOARD_H_INCLUDED
#define BILLBOARD_H_INCLUDED

#include "types.h"
#include "math.h"

/***
 * PURPOSE: Creates new billboard
 *  RETURN: Created billboard identifier if success, 0 otherwise
 *   PARAM: [IN] sceneID   - scene to create billboard
 *   PARAM: [IN] textureID - billboard texture
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEBillboardCreate( const VEUINT sceneID, const VEUINT textureID );

/***
 * PURPOSE: Set billboard's position
 *   PARAM: [IN] sceneID     - scene identifier
 *   PARAM: [IN] billboardID - billboard's identifier
 *   PARAM: [IN] position    - new billboard's position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBillboardSetPosition( const VEUINT sceneID, const VEUINT billboardID, const VEVECTOR3D position );

/***
 * PURPOSE: Set billboard's size
 *   PARAM: [IN] sceneID     - scene identifier
 *   PARAM: [IN] billboardID - billboard's identifier
 *   PARAM: [IN] size        - new billboard's size
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBillboardSetSize( const VEUINT sceneID, const VEUINT billboardID, const VEREAL size );

/***
 * PURPOSE: Deletes existing billboard
 *   PARAM: [IN] sceneID     - scene identifier
 *   PARAM: [IN] billboardID - billboard's identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBillboardDelete( const VEUINT sceneID, const VEUINT billboardID );

#endif // BILLBOARD_H_INCLUDED
