#include "internalterraintrim.h"

/***
 * PURPOSE: Trim faces along top side
 *   PARAM: [IN] terrain - pointer to terrain to render
 *   PARAM: [IN] grid    - Grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimTop( VETERRAINLOD *chunk, VETERRAINGRID *grid )
{
  VEUINT faceID      = 0;
  VEUINT facesPerRow = 2 * (grid->m_Width-1);
  VEUINT faceOffset  = 2 * facesPerRow;

  /* 8--7--6        8--7--6
   * | /| /|        | /| /|
   * |/ |/ |        |/ |/ |
   * 5--4--3  ===>  5--4--3
   * | /| /|        | / \ |
   * |/ |/ |        |/   \|
   * 2--1--0        2-----0
   */

  /* Trimming */
  for (faceID = 0; faceID < chunk->m_NumFaces; faceID += faceOffset)
  {
    VETERRAINFACE face013 = chunk->m_Faces[faceID+0];
    VETERRAINFACE face124 = chunk->m_Faces[faceID+facesPerRow];

    /* 0 -> 4 -> 3 face */
    chunk->m_Faces[faceID].m_V1 = face013.m_V1;
    chunk->m_Faces[faceID].m_V2 = face124.m_V3;
    chunk->m_Faces[faceID].m_V3 = face013.m_V3;

    /* 2 -> 4 -> 0 face */
    chunk->m_Faces[faceID+1].m_V1 = face124.m_V2;
    chunk->m_Faces[faceID+1].m_V2 = face124.m_V3;
    chunk->m_Faces[faceID+1].m_V3 = face013.m_V1;

    /* Remove face 2 -> 4 -> 1  */
    chunk->m_Faces[faceID+facesPerRow].m_V1 = 0;
    chunk->m_Faces[faceID+facesPerRow].m_V2 = 0;
    chunk->m_Faces[faceID+facesPerRow].m_V3 = 0;
  }
} /* End of 'VETerrainTrimTop' function */

/***
 * PURPOSE: Trim faces along bottom side
 *   PARAM: [IN] terrain - pointer to terrain to render
 *   PARAM: [IN] grid    - Grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimBottom( VETERRAINLOD *chunk, VETERRAINGRID *grid )
{
  VEUINT faceID      = 0;
  VEUINT facesPerRow = 2 * (grid->m_Width-1);
  VEUINT faceOffset  = 2 * facesPerRow;

  /* 8--7--6        8-----6
   * | /| /|        |\   /|
   * |/ |/ |        | \ / |
   * 5--4--3  ===>  5--4--3
   * | /| /|        | /| /|
   * |/ |/ |        |/ |/ |
   * 2--1--0        2--1--0
   */

  /* Trimming */
  for (faceID = facesPerRow-2; faceID < chunk->m_NumFaces; faceID += faceOffset)
  {
    VETERRAINFACE face476 = chunk->m_Faces[faceID+1];
    VETERRAINFACE face587 = chunk->m_Faces[faceID+facesPerRow+1];

    /* 4 -> 8 -> 6 face */
    chunk->m_Faces[faceID+1].m_V1 = face476.m_V1;
    chunk->m_Faces[faceID+1].m_V2 = face587.m_V2;
    chunk->m_Faces[faceID+1].m_V3 = face476.m_V3;

    /* 5 -> 8 -> 4 face */
    chunk->m_Faces[faceID+facesPerRow].m_V1 = face587.m_V1;
    chunk->m_Faces[faceID+facesPerRow].m_V2 = face587.m_V2;
    chunk->m_Faces[faceID+facesPerRow].m_V3 = face476.m_V1;

    /* Remove face 5 -> 8 -> 7  */
    chunk->m_Faces[faceID+facesPerRow+1].m_V1 = 0;
    chunk->m_Faces[faceID+facesPerRow+1].m_V2 = 0;
    chunk->m_Faces[faceID+facesPerRow+1].m_V3 = 0;
  }
} /* End of 'VETerrainTrimBottom' function */

/***
 * PURPOSE: Trim faces along left side
 *   PARAM: [IN] terrain - pointer to terrain
 *   PARAM: [IN] grid    - terrain grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimLeft( VETERRAINLOD *chunk, VETERRAINGRID *grid )
{
  VEUINT faceID      = 0;
  VEUINT facesPerRow = 2 * (grid->m_Width-1);

  /* 8--7--6        8--7--6
   * | /| /|        | /| /|
   * |/ |/ |        |/ |/ |
   * 5--4--3  ===>  5--4  |
   * | /| /|        | /|\ |
   * |/ |/ |        |/ | \|
   * 2--1--0        2--1--0
   */

  /* Trimming */
  for (faceID = 0; faceID < facesPerRow; faceID += 4)
  {
    VETERRAINFACE face013 = chunk->m_Faces[faceID+0];
    VETERRAINFACE face346 = chunk->m_Faces[faceID+2];

    /* 0 -> 4 -> 6 face */
    chunk->m_Faces[faceID+0].m_V1 = face013.m_V1;
    chunk->m_Faces[faceID+0].m_V2 = face346.m_V2;
    chunk->m_Faces[faceID+0].m_V3 = face346.m_V3;

    /* 0 -> 1 -> 4 face */
    chunk->m_Faces[faceID+1].m_V1 = face013.m_V1;
    chunk->m_Faces[faceID+1].m_V2 = face013.m_V2;
    chunk->m_Faces[faceID+1].m_V3 = face346.m_V2;

    /* Remove face 3 -> 4 -> 6  */
    chunk->m_Faces[faceID+2].m_V1 = 0;
    chunk->m_Faces[faceID+2].m_V2 = 0;
    chunk->m_Faces[faceID+2].m_V3 = 0;
  }
} /* End of 'VETerrainTrimLeft' function */

/***
 * PURPOSE: Trim faces along right side
 *   PARAM: [IN] terrain - pointer to terrain to render
 *   PARAM: [IN] grid    - Grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimRight( VETERRAINLOD *chunk, VETERRAINGRID *grid )
{
  VEUINT faceID      = 0;
  VEUINT facesPerRow = 2 * (grid->m_Width-1);

  /* 8--7--6        8--7--6
   * | /| /|        |\ | /|
   * |/ |/ |        | \|/ |
   * 5--4--3  ===>  |  4--3
   * | /| /|        | /| /|
   * |/ |/ |        |/ |/ |
   * 2--1--0        2--1--0
   */

  /* Trimming */
  for (faceID = chunk->m_NumFaces - facesPerRow; faceID < chunk->m_NumFaces; faceID += 4)
  {
    VETERRAINFACE face254 = chunk->m_Faces[faceID+1];
    VETERRAINFACE face587 = chunk->m_Faces[faceID+3];

    /* 2 -> 8 -> 4 face */
    chunk->m_Faces[faceID+1].m_V1 = face254.m_V1;
    chunk->m_Faces[faceID+1].m_V2 = face587.m_V2;
    chunk->m_Faces[faceID+1].m_V3 = face254.m_V3;

    /* 8 -> 7 -> 4 face */
    chunk->m_Faces[faceID+2].m_V1 = face587.m_V2;
    chunk->m_Faces[faceID+2].m_V2 = face587.m_V3;
    chunk->m_Faces[faceID+2].m_V3 = face254.m_V3;

    /* Remove face 5 -> 8 -> 7  */
    chunk->m_Faces[faceID+3].m_V1 = 0;
    chunk->m_Faces[faceID+3].m_V2 = 0;
    chunk->m_Faces[faceID+3].m_V3 = 0;
  }
} /* End of 'VETerrainTrimRight' function */

/***
 * PURPOSE: Trim faces along left & top sides
 *   PARAM: [IN] terrain - pointer to terrain
 *   PARAM: [IN] grid    - terrain grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimLeftTop( VETERRAINLOD *chunk, VETERRAINGRID *grid )
{
  VETerrainTrimLeft(chunk, grid);
  VETerrainTrimTop(chunk, grid);
} /* End of 'VETerrainTrimLeftTop' function */

/***
 * PURPOSE: Trim faces along right & top sides
 *   PARAM: [IN] terrain - pointer to terrain
 *   PARAM: [IN] grid    - terrain grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimRightTop( VETERRAINLOD *chunk, VETERRAINGRID *grid )
{
  VETerrainTrimTop(chunk, grid);
  VETerrainTrimRight(chunk, grid);
} /* End of 'VETerrainTrimRightTop' function */

/***
 * PURPOSE: Trim faces along right & bottom sides
 *   PARAM: [IN] terrain - pointer to terrain
 *   PARAM: [IN] grid    - terrain grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimRightBottom( VETERRAINLOD *chunk, VETERRAINGRID *grid )
{
  VEUINT faceID      = 0;
  VEUINT facesPerRow = 2 * (grid->m_Width-1);

  /* Trimming bottom side */
  VETerrainTrimBottom(chunk, grid);

  /* Trimming right side except last one */
  for (faceID = chunk->m_NumFaces - facesPerRow; faceID < chunk->m_NumFaces-4; faceID += 4)
  {
    VETERRAINFACE face254 = chunk->m_Faces[faceID+1];
    VETERRAINFACE face587 = chunk->m_Faces[faceID+3];

    /* 2 -> 8 -> 4 face */
    chunk->m_Faces[faceID+1].m_V1 = face254.m_V1;
    chunk->m_Faces[faceID+1].m_V2 = face587.m_V2;
    chunk->m_Faces[faceID+1].m_V3 = face254.m_V3;

    /* 8 -> 7 -> 4 face */
    chunk->m_Faces[faceID+2].m_V1 = face587.m_V2;
    chunk->m_Faces[faceID+2].m_V2 = face587.m_V3;
    chunk->m_Faces[faceID+2].m_V3 = face254.m_V3;

    /* Remove face 5 -> 8 -> 7  */
    chunk->m_Faces[faceID+3].m_V1 = 0;
    chunk->m_Faces[faceID+3].m_V2 = 0;
    chunk->m_Faces[faceID+3].m_V3 = 0;
  }

  /* Remove unnecessary faces */
  for (faceID = chunk->m_NumFaces-3; faceID < chunk->m_NumFaces; faceID++)
  {
    chunk->m_Faces[faceID].m_V1 = 0;
    chunk->m_Faces[faceID].m_V2 = 0;
    chunk->m_Faces[faceID].m_V3 = 0;
  }
} /* End of 'VETerrainTrimRightBottom' function */

/***
 * PURPOSE: Trim faces along left & bottom sides
 *   PARAM: [IN] terrain - pointer to terrain
 *   PARAM: [IN] grid    - terrain grid
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainTrimLeftBottom( VETERRAINLOD *chunk, VETERRAINGRID *grid )
{
  VETerrainTrimBottom(chunk, grid);
  VETerrainTrimLeft(chunk, grid);
} /* End of 'VETerrainTrimLeftBottom' function */
