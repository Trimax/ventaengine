#ifndef INTERNALTERRAINTRIM_H_INCLUDED
#define INTERNALTERRAINTRIM_H_INCLUDED

#include "internalterraingrid.h"
#include "internalterrain.h"

/*** One side trimmed ***/

/***
 * PURPOSE: Trim faces along top side
 *   PARAM: [IN] terrain - pointer to terrain
 *   PARAM: [IN] grid    - terrain grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimTop( VETERRAINLOD *chunk, VETERRAINGRID *grid );

/***
 * PURPOSE: Trim faces along bottom side
 *   PARAM: [IN] terrain - pointer to terrain
 *   PARAM: [IN] grid    - terrain grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimBottom( VETERRAINLOD *chunk, VETERRAINGRID *grid );

/***
 * PURPOSE: Trim faces along right side
 *   PARAM: [IN] terrain - pointer to terrain
 *   PARAM: [IN] grid    - terrain grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimRight( VETERRAINLOD *chunk, VETERRAINGRID *grid );

/***
 * PURPOSE: Trim faces along left side
 *   PARAM: [IN] terrain - pointer to terrain
 *   PARAM: [IN] grid    - terrain grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimLeft( VETERRAINLOD *chunk, VETERRAINGRID *grid );

/*** Two sides trimmed ***/

/***
 * PURPOSE: Trim faces along left & top sides
 *   PARAM: [IN] terrain - pointer to terrain
 *   PARAM: [IN] grid    - terrain grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimLeftTop( VETERRAINLOD *chunk, VETERRAINGRID *grid );

/***
 * PURPOSE: Trim faces along right & top sides
 *   PARAM: [IN] terrain - pointer to terrain
 *   PARAM: [IN] grid    - terrain grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimRightTop( VETERRAINLOD *chunk, VETERRAINGRID *grid );

/***
 * PURPOSE: Trim faces along right & bottom sides
 *   PARAM: [IN] terrain - pointer to terrain
 *   PARAM: [IN] grid    - terrain grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimRightBottom( VETERRAINLOD *chunk, VETERRAINGRID *grid );

/***
 * PURPOSE: Trim faces along left & bottom sides
 *   PARAM: [IN] terrain - pointer to terrain
 *   PARAM: [IN] grid    - terrain grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimLeftBottom( VETERRAINLOD *chunk, VETERRAINGRID *grid );

#endif // INTERNALTERRAINTRIM_H_INCLUDED
