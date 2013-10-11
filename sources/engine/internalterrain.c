#include "internalglut.h"

#include "internalterraintrim.h"
#include "internalterraingrid.h"
#include "internalbuffer.h"
#include "internalheader.h"
#include "memorymanager.h"
#include "math.h"

#include <stdlib.h>
#include <string.h>
#include <math.h>

/***
 * PURPOSE: Determine if number is power of two
 *  RETURN: TRUE if number is power of two, FALSE otherwise
 *   PARAM: [IN] number - number to check
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsPowerOf2( const VEINT number )
{
  return ((number&(number-1)) == 0);
} /* End of 'VEIsPowerOf2' function */

/***
 * PURPOSE: Determine is height map valid
 *  RETURN: TRUE if height map is valid, FALSE otherwise
 *   PARAM: [IN] heightMap - pointer to height map
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEHeightMapIsValid( const VETEXTURE *heightMap )
{
  /* Wrong argument */
  if (!heightMap)
    return FALSE;

  /* Wrong texture size */
  if ((heightMap->m_Width <= VE_TERRAIN_CHUNKSIZE)||(heightMap->m_Height <= VE_TERRAIN_CHUNKSIZE))
    return FALSE;

  /* Height map should have size: {2^(7+m) x 2^(7+n)}
   * We need it because of chunk division */
  if (VEIsPowerOf2(heightMap->m_Width) && VEIsPowerOf2(heightMap->m_Height))
    return TRUE;
  return FALSE;
} /* End of 'VEHeightMapIsValid' function */

/***
 * PURPOSE: Determine texture coordinates using absolute position
 *  RETURN: Texture coordinates
 *   PARAM: [IN] x - absciss
 *   PARAM: [IN] z - applicate
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR2D VETerrainGetTCoordinate( const VEUINT x, const VEUINT z )
{
  return VEVector2D((VEREAL)(x%VE_TERRAIN_CHUNKSIZE) / ((VEREAL)VE_TERRAIN_CHUNKSIZE),
                    (VEREAL)(z%VE_TERRAIN_CHUNKSIZE) / ((VEREAL)VE_TERRAIN_CHUNKSIZE));
} /* End of 'VETerrainGetTCoordinate' function */

/***
 * PURPOSE: Calculate height using red channel from height map
 *  RETURN: Height at (x, z)
 *   PARAM: [IN] heightMap - height map
 *   PARAM: [IN] x         - absciss
 *   PARAM: [IN] z         - applicate
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VETerrainGetHeightInternal( const VETEXTURE *heightMap, const VEUINT x, const VEUINT z )
{
  return (VEREAL)heightMap->m_Data[4 * (x * heightMap->m_Height + z)] / 255.0;
} /* End of 'VETerrainGetHeightInternal' function */

/***
 * PURPOSE: Build normals using height map
 *   PARAM: [IN/OUT] terrain - pointer to terrain to build normals
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainBuildNormals( VETERRAIN *terrain )
{
  VETEXTURE *hMap = terrain->m_HeightMap;
  VEUINT x = 0, z = 0;

  /* Build normals */
  for (z = 0; z < terrain->m_HeightMap->m_Height; z++)
    for (x = 0; x < terrain->m_HeightMap->m_Width; x++)
    {
      VEREAL sx = VETerrainGetHeightInternal(hMap, x < hMap->m_Width-1 ? x+1 : x, z) - VETerrainGetHeightInternal(hMap, x > 0 ? x-1 : x, z);
      VEREAL sz = VETerrainGetHeightInternal(hMap, x, z < hMap->m_Height-1 ? z+1 : z) - VETerrainGetHeightInternal(hMap, x, z > 0 ? z-1 : z);

      if (x == 0 || x == hMap->m_Width-1)
        sx *= 2;

      if (z == 0 || z == hMap->m_Height-1)
        sz *= 2;

      terrain->m_Vertices[z * hMap->m_Width + x].m_Normal = VEVector3DNormalize(VEVector3D(-sx, 2, -sz));
    }
} /* End of 'VETerrainBuildNormals' function */

/***
 * PURPOSE: Prepare terrain vertices using height map
 *   PARAM: [IN/OUT] terrain - pointer to terrain to prepare vertices
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETerrainPrepareVertices( VETERRAIN *terrain )
{
  VEUINT x = 0, z = 0;

  /* Extend landscape (we need to to this for chunks division) */
  terrain->m_Width = terrain->m_HeightMap->m_Width+1;
  terrain->m_Height = terrain->m_HeightMap->m_Height+1;

  /* Memory allocation */
  terrain->m_Vertices = New(sizeof(VEVERTEX) * terrain->m_Width * terrain->m_Height, "Terrain vertices");
  if (!terrain->m_Vertices)
    return FALSE;

  for (z = 0; z < terrain->m_Height; z++)
    for (x = 0; x < terrain->m_Width; x++)
    {
      VEUINT origX = VEMIN(x, terrain->m_Width-2), origZ = VEMIN(z, terrain->m_Height-2);
      VEUINT vertexIDSrc = origZ * terrain->m_HeightMap->m_Width + origX;
      VEUINT vertexIDDst = z * terrain->m_Height + x;

      /* Take height from ALPHA channel */
      VEREAL height = terrain->m_ScaleFactor.m_Y * ((VEREAL)terrain->m_HeightMap->m_Data[4 * vertexIDSrc + 3] - 128.0) / 128.0;

      /* Fill vertex */
      terrain->m_Vertices[vertexIDDst].m_TCoordionates = VEVector2D(x, z); // VETerrainGetTCoordinate(x, z);
      terrain->m_Vertices[vertexIDDst].m_Position      = VEVector3D(x * terrain->m_ScaleFactor.m_X, height, z * terrain->m_ScaleFactor.m_Z);
      terrain->m_Vertices[vertexIDDst].m_Color         = VEColorToVector4D(VECOLOR_GREEN);
    }

  /* Building normals */
  VETerrainBuildNormals(terrain);

  /* Prepare VBO vertices buffer */
  glGenBuffers(1, &terrain->m_BufferID);
  glBindBuffer(GL_ARRAY_BUFFER, terrain->m_BufferID);

  /* Prepare memory block to store data */
  glBufferData(GL_ARRAY_BUFFER, sizeof(VEVERTEX) * terrain->m_Width * terrain->m_Height, terrain->m_Vertices, GL_STATIC_DRAW);
  glBindBuffer(GL_ARRAY_BUFFER, 0);

  /* That's it */
  return TRUE;
} /* End of 'VETerrainPrepareVertices' function */

/***
 * PURPOSE: Prepare terrain chunk LOD using vertices array
 *   PARAM: [IN/OUT] terrain  - pointer to terrain to prepare chunk
 *   PARAM: [IN] x            - chunk absciss
 *   PARAM: [IN] z            - chunk applicate
 *   PARAM: [IN]     levelLOD - current LOD level
 *   PARAM: [IN]     trimType - current trimming type
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETerrainPrepareLOD( VETERRAIN *terrain, const VEUINT chunkX, const VEUINT chunkZ, const VEUINT levelLOD, const VEUINT trimType )
{
  VEUINT x = 0, z = 0, faceID = 0;
  VETERRAINLOD *chunk = &terrain->m_Chunks[chunkX][chunkZ].m_LODs[levelLOD][trimType];
  VETERRAINGRID *grid = VETerrainGridCreate(terrain, levelLOD, chunkX, chunkZ);
  if (!grid)
    return FALSE;

  /* Build faces using grid */
  chunk->m_NumFaces = 2 * (grid->m_Width - 1) * (grid->m_Height - 1);
  chunk->m_Faces = New(sizeof(VETERRAINFACE) * chunk->m_NumFaces, "Chunk LOD faces");
  if (!chunk->m_Faces)
  {
    VETerrainGridDelete(grid);
    return FALSE;
  }

  /* Building faces according to scheme (X to right, Z to down)
   * 3--4
   * | /|
   * |/ |
   * 2--1
   */
   for (x = 0; x < grid->m_Width-1; x++)
     for (z = 0; z < grid->m_Height-1; z++)
     {
       /* 1 -> 2 -> 4 face */
       chunk->m_Faces[faceID].m_V1 = grid->m_Vertices[x+0][z+0];
       chunk->m_Faces[faceID].m_V2 = grid->m_Vertices[x+1][z+0];
       chunk->m_Faces[faceID].m_V3 = grid->m_Vertices[x+0][z+1];
       faceID++;

       /* 2 -> 3 -> 4 face */
       chunk->m_Faces[faceID].m_V1 = grid->m_Vertices[x+1][z+0];
       chunk->m_Faces[faceID].m_V2 = grid->m_Vertices[x+1][z+1];
       chunk->m_Faces[faceID].m_V3 = grid->m_Vertices[x+0][z+1];
       faceID++;
     }

  /* Trimming edges */
  switch(trimType)
  {
    /* One-side trimming */
  case VE_TERRAIN_TRIMLEFT:
    VETerrainTrimLeft(chunk, grid);
    break;
  case VE_TERRAIN_TRIMRIGHT:
    VETerrainTrimRight(chunk, grid);
    break;
  case VE_TERRAIN_TRIMTOP:
    VETerrainTrimTop(chunk, grid);
    break;
  case VE_TERRAIN_TRIMBOTTOM:
    VETerrainTrimBottom(chunk, grid);
    break;

    /* Two-side trimming */
  case VE_TERRAIN_TRIMLEFTTOP:
    VETerrainTrimLeftTop(chunk, grid);
    break;
  case VE_TERRAIN_TRIMRIGHTTOP:
    VETerrainTrimRightTop(chunk, grid);
    break;
  case VE_TERRAIN_TRIMRIGHTBOTTOM:
    VETerrainTrimRightBottom(chunk, grid);
    break;
  case VE_TERRAIN_TRIMLEFTBOTTOM:
    VETerrainTrimLeftBottom(chunk, grid);
    break;
  }

  /* Release memory */
  VETerrainGridDelete(grid);

  /* Prepare index buffer*/
  glGenBuffers(1, &chunk->m_BufferID);
  glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, chunk->m_BufferID);
  glBufferData(GL_ELEMENT_ARRAY_BUFFER, chunk->m_NumFaces * sizeof(VETERRAINFACE), chunk->m_Faces, GL_STATIC_DRAW);
  glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

  /* That's it */
  return TRUE;
} /* End of 'VETerrainPrepareLOD' function */

/***
 * PURPOSE: Select minimal components of two vectors
 *  RETURN: Vector with the minimal components
 *   PARAM: [IN] v1 - first vector
 *   PARAM: [IN] v2 - second vector
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DLowest( const VEVECTOR3D v1, const VEVECTOR3D v2 )
{
  return VEVector3D(VEMIN(v1.m_X, v2.m_X), VEMIN(v1.m_Y, v2.m_Y), VEMIN(v1.m_Z, v2.m_Z));
} /* End of 'VEVector3DLowest' function */

/***
 * PURPOSE: Select maximal components of two vectors
 *  RETURN: Vector with the maximal components
 *   PARAM: [IN] v1 - first vector
 *   PARAM: [IN] v2 - second vector
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DHighest( const VEVECTOR3D v1, const VEVECTOR3D v2 )
{
  return VEVector3D(VEMAX(v1.m_X, v2.m_X), VEMAX(v1.m_Y, v2.m_Y), VEMAX(v1.m_Z, v2.m_Z));
} /* End of 'VEVector3DHighest' function */

/***
 * PURPOSE: Prepare terrain chunk using vertices array
 *  RETURN: Chunk AABB
 *   PARAM: [IN] terrain - pointer to terrain
 *   PARAM: [IN] chunk   - pointer to the most detailed LOD of chunk
 *  AUTHOR: Eliseev Dmitry
 ***/
VEAABB VETerrainBuildAABB( const VETERRAIN *terrain, const VETERRAINLOD *chunk )
{
  VEAABB AABB = VEAABBCreate(terrain->m_Vertices[chunk->m_Faces[0].m_V1].m_Position, terrain->m_Vertices[chunk->m_Faces[0].m_V1].m_Position);
  VEUINT faceID = 0;

  /* For each face */
  for (faceID = 0; faceID < chunk->m_NumFaces; faceID++)
  {
    VEVECTOR3D v1 = terrain->m_Vertices[chunk->m_Faces[faceID].m_V1].m_Position;
    VEVECTOR3D v2 = terrain->m_Vertices[chunk->m_Faces[faceID].m_V2].m_Position;
    VEVECTOR3D v3 = terrain->m_Vertices[chunk->m_Faces[faceID].m_V3].m_Position;

    /* Determine lowest point */
    AABB.m_Min = VEVector3DLowest(AABB.m_Min, v1);
    AABB.m_Min = VEVector3DLowest(AABB.m_Min, v2);
    AABB.m_Min = VEVector3DLowest(AABB.m_Min, v3);

    /* Determine highest point */
    AABB.m_Max = VEVector3DHighest(AABB.m_Max, v1);
    AABB.m_Max = VEVector3DHighest(AABB.m_Max, v2);
    AABB.m_Max = VEVector3DHighest(AABB.m_Max, v3);
  }

  /* That's it */
  return AABB;
} /* End of 'VETerrainBuildAABB' function */

/***
 * PURPOSE: Prepare terrain chunk using vertices array
 *   PARAM: [IN/OUT] terrain - pointer to terrain to prepare chunk
 *   PARAM: [IN] x           - chunk absciss
 *   PARAM: [IN] z           - chunk applicate
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETerrainPrepareChunk( VETERRAIN *terrain, const VEUINT chunkX, const VEUINT chunkZ )
{
  VETERRAINCHUNK *chunk = &terrain->m_Chunks[chunkX][chunkZ];
  VEUINT levelLOD = 0, trimType = 0;

  /* Allocate memory for LODs */
  chunk->m_LODs = New(sizeof(VETERRAINLOD*) * VE_TERRAIN_NUMLOD, "Terrain chunk LODs");
  if (!chunk->m_LODs)
    return FALSE;

  /* Allocate memory for each trimming type */
  for (levelLOD = 0; levelLOD < VE_TERRAIN_NUMLOD; levelLOD++)
  {
    chunk->m_LODs[levelLOD] = New(sizeof(VETERRAINLOD) * VE_TERRAIN_NUMTRIMS, "Terrain chunk LOD");
    if (!chunk->m_LODs[levelLOD])
      return FALSE;
  }

  /* Prepare LODs */
  for (levelLOD = 0; levelLOD < VE_TERRAIN_NUMLOD; levelLOD++)
    for (trimType = 0; trimType < VE_TERRAIN_NUMTRIMS; trimType++)
      if (!VETerrainPrepareLOD(terrain, chunkX, chunkZ, levelLOD, trimType))
        return FALSE;

  /* Build AABB using the most detailed LOD */
  chunk->m_AABB = VETerrainBuildAABB(terrain, &chunk->m_LODs[0][VE_TERRAIN_NOTRIM]);

  /* That' it */
  return TRUE;
} /* End of 'VETerrainPrepareChunk' function */

/***
 * PURPOSE: Prepare terrain chunks using vertices array
 *   PARAM: [IN/OUT] terrain - pointer to terrain to prepare chunks
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETerrainPrepareChunks( VETERRAIN *terrain )
{
  VEUINT chunkX = 0, chunkZ = 0;

  /* Allocate memory */
  terrain->m_Chunks = New(sizeof(VETERRAINCHUNK*) * terrain->m_ChunksX, "Terrain chunks row");
  if (!terrain->m_Chunks)
    return FALSE;

  /* Allocate chunk rows */
  for (chunkX = 0; chunkX < terrain->m_ChunksX; chunkX++)
  {
    terrain->m_Chunks[chunkX] = New(sizeof(VETERRAINCHUNK) * terrain->m_ChunksZ, "Terrain chunks");
    if (!terrain->m_Chunks[chunkX])
      return FALSE;
  }

  /* Prepare each chunk */
  for (chunkX = 0; chunkX < terrain->m_ChunksX; chunkX++)
    for (chunkZ = 0; chunkZ < terrain->m_ChunksZ; chunkZ++)
      if (!VETerrainPrepareChunk(terrain, chunkX, chunkZ))
        return FALSE;

  /* That's it */
  return TRUE;
} /* End of 'VETerrainPrepareChunks' function */

/***
 * PURPOSE: Prepare terrain to render. Build LOD, create chunks, etc...
 *   PARAM: [IN/OUT] terrain - pointer to terrain to render
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETerrainPrepareInternal( VETERRAIN *terrain )
{
  terrain->m_ChunksX = terrain->m_HeightMap->m_Width / VE_TERRAIN_CHUNKSIZE;
  terrain->m_ChunksZ = terrain->m_HeightMap->m_Height / VE_TERRAIN_CHUNKSIZE;

  /* Prepare vertices */
  if (!VETerrainPrepareVertices(terrain))
    return FALSE;

  /* Prepare chunks */
  if (!VETerrainPrepareChunks(terrain))
    return FALSE;

  /* That's it */
  return TRUE;
} /* End of 'VETerrainPrepareInternal' function */

/***
 * PURPOSE: Create terrain file using separate files
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN]     mapBH - blend map file with height at alpha channel
 *   PARAM: [IN]      map1 - color map #1
 *   PARAM: [IN]      map2 - color map #2
 *   PARAM: [IN]      map3 - color map #3
 *   PARAM: [IN]      map4 - color map #4
 *   PARAM: [IN]    output - output file
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETerrainBuildInternal( const VEBUFFER mapBH, const VEBUFFER map1, const VEBUFFER map2,  const VEBUFFER map3,  const VEBUFFER map4,  const VEBUFFER output )
{
  VEHEADER header;
  FILE *outputFile = NULL;

  /* Prepare header */
  memset(&header, 0, sizeof(VEHEADER));
  header.m_Prefix  = 'V';
  header.m_Format = VE_FORMAT_TERRAIN;
  header.m_Version = VE_FORMATVERSION;

  /* Write data */
  outputFile = fopen(output, "wb");
  fwrite(&header, 1, sizeof(VEHEADER), outputFile);

  /* Writing maps */
  VEBufferWrite(outputFile, mapBH);
  VEBufferWrite(outputFile, map1);
  VEBufferWrite(outputFile, map2);
  VEBufferWrite(outputFile, map3);
  VEBufferWrite(outputFile, map4);

  /* That's it */
  fclose(outputFile);
  return TRUE;
} /* End of 'VETerrainBuildInternal' function */
