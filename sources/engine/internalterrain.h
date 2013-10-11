#ifndef INTERNALTERRAIN_H_INCLUDED
#define INTERNALTERRAIN_H_INCLUDED

#include "internaltexturemanager.h"
#include "internalcamera.h"
#include "internalaabb.h"
#include "internalmesh.h"
#include "vector3d.h"
#include "types.h"

/* Terrain chunk size */
#define VE_TERRAIN_CHUNKSIZE   128
#define VE_TERRAIN_CHUNKSIZE_S "128"

/* Number of LOD's */
#define VE_TERRAIN_NUMLOD 7

/*** Sides trimming ***/

/* Number of trimmings */
#define VE_TERRAIN_TRIMALL 9
#define VE_TERRAIN_NUMTRIMS (VE_TERRAIN_TRIMALL+1)

/* Trimmings */
#define VE_TERRAIN_NOTRIM          0
#define VE_TERRAIN_TRIMLEFT        1
#define VE_TERRAIN_TRIMTOP         2
#define VE_TERRAIN_TRIMRIGHT       3
#define VE_TERRAIN_TRIMBOTTOM      4
#define VE_TERRAIN_TRIMLEFTTOP     5
#define VE_TERRAIN_TRIMRIGHTTOP    6
#define VE_TERRAIN_TRIMRIGHTBOTTOM 7
#define VE_TERRAIN_TRIMLEFTBOTTOM  8

/* Terrain face */
typedef struct tagVETERRAINFACE
{
  VEUINT m_V1;
  VEUINT m_V2;
  VEUINT m_V3;
} VETERRAINFACE;

/* Terrain LOD */
typedef struct tagVETERRAINLOD
{
  VEUINT         m_NumFaces; /* The number of faces */
  VETERRAINFACE *m_Faces;    /* Faces array */
  VEUINT         m_BufferID; /* Indexes buffer identifier */
} VETERRAINLOD;

/* Terrain chunk */
typedef struct tagVETERRAINCHUNK
{
  VETERRAINLOD **m_LODs;       /* LODs array LOD level x Trimming type (10) */
  VEAABB         m_AABB;       /* Chunk bounding box */
  VEBYTE         m_TrimType;   /* Trimming type */
  VEBYTE         m_LevelLOD;   /* Current LOD */
  VEBOOL         m_IsVisisble; /* Visibility flag */
} VETERRAINCHUNK;

/* Terrain shader parameters */
typedef struct tagVETERRAINSHADER
{
  VEINT m_ShaderMapBlend; /* Shader blend map location */
  VEINT m_ShaderMap1;     /* Shader map #1 location */
  VEINT m_ShaderMap2;     /* Shader map #2 location */
  VEINT m_ShaderMap3;     /* Shader map #3 location */
  VEINT m_ShaderMap4;     /* Shader map #4 location */

  VEINT m_ShaderParams;   /* Shader parameters */
} VETERRAINSHADER;

/* Terrain textures stack */
typedef struct tagVETERRAINTEXTURE
{
  VEUINT m_MapBlend; /* Blend map texture identifier */
  VEUINT m_Map1;     /* Map texture identifier #1 */
  VEUINT m_Map2;     /* Map texture identifier #2 */
  VEUINT m_Map3;     /* Map texture identifier #3 */
  VEUINT m_Map4;     /* Map texture identifier #4 */
} VETERRAINTEXTURE;

/* Terrain structure */
typedef struct tagVETERRAIN
{
  VEUINT            m_ProgramID;   /* Linked GPU program identifier */
  VEUINT            m_ChunksX;     /* Number of chunks along X */
  VEUINT            m_ChunksZ;     /* Number of chunks along Z */
  VEUINT            m_ChunkX;      /* Current chunk X */
  VEUINT            m_ChunkZ;      /* Current chunk Z */
  VEVERTEX         *m_Vertices;    /* Vertices */
  VETERRAINCHUNK  **m_Chunks;      /* Chunks */
  VETERRAINTEXTURE  m_Texture;     /* Terrain texture */
  VETERRAINSHADER   m_Shader;      /* Terrain shader paramters */
  VETEXTURE        *m_HeightMap;   /* Terrain height map */
  VEUINT            m_BufferID;    /* Vertices buffer identifier */
  VEUINT            m_Width;       /* Terrain width (must be (2^(7+n) + 1)) */
  VEUINT            m_Height;      /* Terrain height (must be (2^(7+m) + 1))  */
  VEVECTOR3D        m_ScaleFactor; /* Scale factor */
  VEBOOL            m_IsWired;     /* Wired rendering */
} VETERRAIN;

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
VEBOOL VETerrainBuildInternal( const VEBUFFER mapBH, const VEBUFFER map1, const VEBUFFER map2,  const VEBUFFER map3,  const VEBUFFER map4,  const VEBUFFER output );

/***
 * PURPOSE: Render terrain
 *   PARAM: [IN] terrain - pointer to terrain to render
 *   PARAM: [IN] camera  - pointer to currenct camera
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainRenderInternal( VETERRAIN *terrain, const VECAMERA *camera );

/***
 * PURPOSE: Prepare terrain to render. Build LOD, create chunks, etc...
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN/OUT] terrain - pointer to terrain to render
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETerrainPrepareInternal( VETERRAIN *terrain );

/***
 * PURPOSE: Delete terrain
 *   PARAM: [IN] terrain - pointer to terrain to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainDeleteInternal( VETERRAIN *terrain );

/***
 * PURPOSE: Determine is height map valid
 *  RETURN: TRUE if height map is valid, FALSE otherwise
 *   PARAM: [IN] heightMap - pointer to height map
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEHeightMapIsValid( const VETEXTURE *heightMap );

/***
 * PURPOSE: Determine maximal number of LODs
 *  RETURN: Maximal number of LODs
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VETerrainLODGetNumber( VEVOID );

#endif // INTERNALTERRAIN_H_INCLUDED
