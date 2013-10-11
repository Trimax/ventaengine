#ifndef TERRAIN_H_INCLUDED
#define TERRAIN_H_INCLUDED

#include "vector3d.h"
#include "types.h"

/***
 * PURPOSE: Load terrain into scene
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] sceneID     - scene identifier to render
 *   PARAM: [IN] terrainFile - terrain file name (height map file should be {2^(7+n) x 2^(7+m)})
 *   PARAM: [IN] scaleFactor - scale factor along dimensions
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETerrainLoad( const VEUINT sceneID, const VEBUFFER terrainFile, const VEVECTOR3D scaleFactor );

/***
 * PURPOSE: Set terrain rendering type
 *   PARAM: [IN] sceneID - scene identifier to render
 *   PARAM: [IN] isWired - wired rendering flag
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainSetWired( const VEUINT sceneID, const VEBOOL isWired );

/***
 * PURPOSE: Unload terrain from scene
 *   PARAM: [IN] sceneID - scene identifier to render
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainUnload( const VEUINT sceneID );

#endif // TERRAIN_H_INCLUDED
