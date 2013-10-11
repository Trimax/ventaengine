#ifndef INTERNALTERRAINGRID_H_INCLUDED
#define INTERNALTERRAINGRID_H_INCLUDED

#include "internalterrain.h"

/* Terrain grid (built using cutting vertices) */
typedef struct tagVETERRAINGRID
{
  VEUINT   m_Width;
  VEUINT   m_Height;
  VEUINT **m_Vertices;
} VETERRAINGRID;

/***
 * PURPOSE: Create new terrain grid by LOD level using terrain
 *  RETURN: Pointer to created terrain grid if success, NULL otherwise
 *   PARAM: [IN] terrain  - pointer to terrain to render
 *   PARAM: [IN] levelLOD - current LOD level
 *   PARAM: [IN] x        - chunk absciss
 *   PARAM: [IN] z        - chunk applicate
 *  AUTHOR: Eliseev Dmitry
 ***/
VETERRAINGRID *VETerrainGridCreate( VETERRAIN *terrain, const VEUINT levelLOD, const VEUINT chunkX, const VEUINT chunkZ );

/***
 * PURPOSE: Delete terrain grid
 *   PARAM: [IN] grid - pointer to grid to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainGridDelete( VETERRAINGRID *grid );

/***
 * PURPOSE: Write vertices info to HDD
 *   PARAM: [IN] filename - file to write dump
 *   PARAM: [IN] terrain  - terrain to dump
 *   PARAM: [IN] grid     - grid information
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainGridDump( const VEBUFFER filename, const VETERRAIN *terrain, const VETERRAINGRID *grid );

#endif // INTERNALTERRAINGRID_H_INCLUDED
