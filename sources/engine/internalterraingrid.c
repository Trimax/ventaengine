#include "internalterraingrid.h"
#include "memorymanager.h"

/***
 * PURPOSE: Create new terrain grid by LOD level using terrain
 *  RETURN: Pointer to created terrain grid if success, NULL otherwise
 *   PARAM: [IN] terrain  - pointer to terrain to render
 *   PARAM: [IN] levelLOD - current LOD level
 *   PARAM: [IN] x        - chunk absciss
 *   PARAM: [IN] z        - chunk applicate
 *  AUTHOR: Eliseev Dmitry
 ***/
VETERRAINGRID *VETerrainGridCreate( VETERRAIN *terrain, const VEUINT levelLOD, const VEUINT chunkX, const VEUINT chunkZ )
{
  VEUINT x = 0, z = 0;
  VETERRAINGRID *grid = New(sizeof(VETERRAINGRID), "Terrain grid");
  if (!grid)
    return NULL;

  /* Determine grid size */
  grid->m_Width  = (VE_TERRAIN_CHUNKSIZE >> levelLOD) + 1;
  grid->m_Height = (VE_TERRAIN_CHUNKSIZE >> levelLOD) + 1;

  /* Allocate memory */
  grid->m_Vertices = New(sizeof(VEUINT*) * grid->m_Width, "Terrain grid rows");
  if (!grid->m_Vertices)
  {
    VETerrainGridDelete(grid);
    return NULL;
  }

  /* Allocate memory for each grid row */
  for (x = 0; x < grid->m_Width; x++)
  {
    grid->m_Vertices[x] = New(sizeof(VEUINT) * grid->m_Height, "Terrain grid row");
    if (!grid->m_Vertices[x])
    {
      VETerrainGridDelete(grid);
      return NULL;
    }
  }

  /* Fill grid with indexes */
  for (x = 0; x < grid->m_Width; x++)
    for (z = 0; z < grid->m_Height; z++)
    {
      VEUINT realX = chunkX * VE_TERRAIN_CHUNKSIZE + (1 << levelLOD) * x;
      VEUINT realZ = chunkZ * VE_TERRAIN_CHUNKSIZE + (1 << levelLOD) * z;

      grid->m_Vertices[x][z] = realZ * terrain->m_Width + realX;
    }

  /* That's it */
  return grid;
} /* End of 'VETerrainGridCreate' function */

/***
 * PURPOSE: Delete terrain grid
 *   PARAM: [IN] grid - pointer to grid to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainGridDelete( VETERRAINGRID *grid )
{
  VEUINT x = 0;

  if (!grid)
    return;

  if (grid->m_Vertices)
    for (x = 0; x < grid->m_Width; x++)
      Delete(grid->m_Vertices[x]);
  Delete(grid->m_Vertices);
  Delete(grid);
} /* End of 'VETerrainGridDelete' function */

/***
 * PURPOSE: Write vertices info to HDD
 *   PARAM: [IN] filename - file to write dump
 *   PARAM: [IN] terrain  - terrain to dump
 *   PARAM: [IN] grid     - grid information
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainGridDump( const VEBUFFER filename, const VETERRAIN *terrain, const VETERRAINGRID *grid )
{
  VEUINT x = 0, z = 0;

  /* Write data */
  FILE *out = fopen(filename, "wt");
  for (x = 0; x < grid->m_Width; x++)
  {
    for (z = 0; z < grid->m_Height; z++)
    {
      VEVECTOR3D vertex = terrain->m_Vertices[grid->m_Vertices[x][z]].m_Position;
      fprintf(out, "%.2f %.2f %.2f;   ", vertex.m_X, vertex.m_Y, vertex.m_Z);
    }
    fprintf(out, "\n");
  }

  /* That's it */
  fclose(out);
} /* End of 'VETerrainChunkDump' function */
