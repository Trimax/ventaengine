#include "internalglut.h"

#include "internalresourcemanager.h"
#include "internalshadermanager.h"
#include "internalscenemanager.h"
#include "internalsettings.h"
#include "internalgeometry.h"
#include "internalculling.h"
#include "internalterrain.h"
#include "internalbuffer.h"
#include "internalheader.h"
#include "texturemanager.h"
#include "memorymanager.h"
#include "internalfs.h"
#include "console.h"
#include "terrain.h"
#include "math.h"

#include <string.h>
#include <stdlib.h>
#include <math.h>

/***
 * PURPOSE: Create a terrain and load height map from a texture
 *  RETURN: Allocated terrain data
 *   PARAM: [IN] terrainFile - terrain file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VETERRAIN *VETerrainCreateInternal( VEBUFFER terrainFile )
{
  VEBUFFER mapBH = NULL, map1 = NULL, map2 = NULL, map3 = NULL, map4 = NULL;
  VEHEADER header;
  VEFILE *mapFile = NULL;
  VETERRAIN *terrain = New(sizeof(VETERRAIN), "Terrain");
  if (!terrain)
    return NULL;

  /* Apply terrain shader program */
  terrain->m_ProgramID = p_SettingsShaderTerrain[VE_SHADER_PROGRAM];

  /* Try to open file */
  mapFile = VEResourcePtrOpen(terrainFile);
  if (!mapFile)
  {
    Delete(terrain);
    return NULL;
  }

  /* Reading terrain header */
  fread(&header, 1, sizeof(VEHEADER), mapFile->m_Ptr);
  if (!VEHeaderIsValid(header, VE_FORMAT_TERRAIN))
  {
    VEResourcePtrClose(mapFile);
    Delete(terrain);
    return NULL;
  }

  /*** Loading maps names  ***/

  /* Load height map */
  mapBH = VEBufferRead(mapFile->m_Ptr);
  if (!mapBH)
  {
    VEResourcePtrClose(mapFile);
    Delete(terrain);
    return NULL;
  }

  /* Load map #1 */
  map1 = VEBufferRead(mapFile->m_Ptr);
  if (!map1)
  {
    Delete(mapBH);
    VEResourcePtrClose(mapFile);
    Delete(terrain);
    return NULL;
  }

  /* Load map #2 */
  map2 = VEBufferRead(mapFile->m_Ptr);
  if (!map2)
  {
    Delete(map1);
    Delete(mapBH);
    VEResourcePtrClose(mapFile);
    Delete(terrain);
    return NULL;
  }

  /* Load map #3 */
  map3 = VEBufferRead(mapFile->m_Ptr);
  if (!map3)
  {
    Delete(map2);
    Delete(map1);
    Delete(mapBH);
    VEResourcePtrClose(mapFile);
    Delete(terrain);
    return NULL;
  }

  /* Load map #4 */
  map4 = VEBufferRead(mapFile->m_Ptr);
  if (!map4)
  {
    Delete(map3);
    Delete(map2);
    Delete(map1);
    Delete(mapBH);
    VEResourcePtrClose(mapFile);
    Delete(terrain);
    return NULL;
  }

  /* Close file */
  VEResourcePtrClose(mapFile);

  /*** Loading maps ***/

  /* Try to open file */
  mapFile = VEResourcePtrOpen(mapBH);
  if (!mapFile)
  {
    Delete(map3);
    Delete(map2);
    Delete(map1);
    Delete(mapBH);
    VEResourcePtrClose(mapFile);
    Delete(terrain);
    return NULL;
  }

  /* Load heightmap */
  if (VEBufferIsEndsWith(mapBH, ".tga"))
    terrain->m_HeightMap = VETextureLoadTGA(mapFile->m_Ptr);
  else if (VEBufferIsEndsWith(mapBH, ".vet"))
    terrain->m_HeightMap = VETextureLoadVET(mapFile->m_Ptr);

  /* Close height map */
  VEResourcePtrClose(mapFile);

  /* Wrong height map */
  if (!terrain->m_HeightMap)
  {
    Delete(map4);
    Delete(map3);
    Delete(map2);
    Delete(map1);
    Delete(mapBH);
    Delete(terrain);
    return NULL;
  }

  /* Load blendmap */
  terrain->m_Texture.m_MapBlend = VETextureLoad(mapBH);
  if (!terrain->m_Texture.m_MapBlend)
  {
    VEConsolePrintArg("Can't load BH-map: ", mapBH);
    Delete(map4);
    Delete(map3);
    Delete(map2);
    Delete(map1);
    Delete(mapBH);
    VETextureUnload(terrain->m_HeightMap);
    Delete(terrain);
    return NULL;
  }

  /* Map #1 */
  terrain->m_Texture.m_Map1 = VETextureLoad(map1);
  if (!terrain->m_Texture.m_Map1)
  {
    VEConsolePrintArg("Can't load map: ", map1);
    VETextureDelete(terrain->m_Texture.m_MapBlend);
    Delete(map4);
    Delete(map3);
    Delete(map2);
    Delete(map1);
    Delete(mapBH);
    VETextureUnload(terrain->m_HeightMap);
    Delete(terrain);
    return NULL;
  }

  /* Map #2 */
  terrain->m_Texture.m_Map2 = VETextureLoad(map2);
  if (!terrain->m_Texture.m_Map2)
  {
    VEConsolePrintArg("Can't load map: ", map2);
    VETextureDelete(terrain->m_Texture.m_Map1);
    VETextureDelete(terrain->m_Texture.m_MapBlend);
    Delete(map4);
    Delete(map3);
    Delete(map2);
    Delete(map1);
    Delete(mapBH);
    VETextureUnload(terrain->m_HeightMap);
    Delete(terrain);
    return NULL;
  }

  /* Map #3 */
  terrain->m_Texture.m_Map3 = VETextureLoad(map3);
  if (!terrain->m_Texture.m_Map3)
  {
    VEConsolePrintArg("Can't load map: ", map3);
    VETextureDelete(terrain->m_Texture.m_Map2);
    VETextureDelete(terrain->m_Texture.m_Map1);
    VETextureDelete(terrain->m_Texture.m_MapBlend);
    Delete(map4);
    Delete(map3);
    Delete(map2);
    Delete(map1);
    Delete(mapBH);
    VETextureUnload(terrain->m_HeightMap);
    Delete(terrain);
    return NULL;
  }

  /* Map #4 */
  terrain->m_Texture.m_Map4 = VETextureLoad(map4);
  if (!terrain->m_Texture.m_Map4)
  {
    VEConsolePrintArg("Can't load map: ", map4);
    VETextureDelete(terrain->m_Texture.m_Map3);
    VETextureDelete(terrain->m_Texture.m_Map2);
    VETextureDelete(terrain->m_Texture.m_Map1);
    VETextureDelete(terrain->m_Texture.m_MapBlend);
    Delete(map4);
    Delete(map3);
    Delete(map2);
    Delete(map1);
    Delete(mapBH);
    VETextureUnload(terrain->m_HeightMap);
    Delete(terrain);
    return NULL;
  }

  /* Delete maps names */
  Delete(map4);
  Delete(map3);
  Delete(map2);
  Delete(map1);
  Delete(mapBH);

  /* Binding shader attributes */
  terrain->m_Shader.m_ShaderMapBlend = glGetUniformLocation(terrain->m_ProgramID, VE_SHADERPARAM_MAPBLEND);
  terrain->m_Shader.m_ShaderMap1     = glGetUniformLocation(terrain->m_ProgramID, VE_SHADERPARAM_MAP1);
  terrain->m_Shader.m_ShaderMap2     = glGetUniformLocation(terrain->m_ProgramID, VE_SHADERPARAM_MAP2);
  terrain->m_Shader.m_ShaderMap3     = glGetUniformLocation(terrain->m_ProgramID, VE_SHADERPARAM_MAP3);
  terrain->m_Shader.m_ShaderMap4     = glGetUniformLocation(terrain->m_ProgramID, VE_SHADERPARAM_MAP4);

  /* Variable with texture parameters */
  terrain->m_Shader.m_ShaderParams = glGetUniformLocation(terrain->m_ProgramID, VE_SHADERPARAM_PARAMETERS);

  /* That's it */
  return terrain;
} /* End of 'VETerrainCreateInternal' function */

/***
 * PURPOSE: Load terrain into scene
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] sceneID     - scene identifier to render
 *   PARAM: [IN] terrainFile - terrain file name (height map file should be {2^(7+n) x 2^(7+m)})
 *   PARAM: [IN] scaleFactor - scale factor along dimensions
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETerrainLoad( const VEUINT sceneID, const VEBUFFER terrainFile, const VEVECTOR3D scaleFactor )
{
  VESCENE *scene = NULL;

  /* Enter critical section */
  VESectionEnterInternal(p_SettingsGraphicsSection);

  /* Obtain scene */
  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return FALSE;
  }

  /* Terrain already was loaded */
  if (scene->m_Terrain)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return FALSE;
  }

  /* Load terrain from height map */
  scene->m_Terrain = VETerrainCreateInternal(terrainFile);
  if (!scene->m_Terrain)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return FALSE;
  }

  /* Storing scale factor */
  scene->m_Terrain->m_ScaleFactor = scaleFactor;

  /* Prepare terrain (create chunks, building LODs, etc...) */
  if (!VETerrainPrepareInternal(scene->m_Terrain))
  {
    VETerrainDeleteInternal(scene->m_Terrain);
    scene->m_Terrain = NULL;
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return FALSE;
  }

  /* That's it */
  VESectionLeaveInternal(p_SettingsGraphicsSection);
  return TRUE;
} /* End of 'VETerrainLoad' function */

/***
 * PURPOSE: Delete terrain
 *   PARAM: [IN] terrain - pointer to terrain to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainDeleteInternal( VETERRAIN *terrain )
{
  VEUINT x = 0, z = 0, levelLOD = 0, trimType = 0;

  if (!terrain)
    return;

  /* Delete buffer */
  if (terrain->m_BufferID)
    glDeleteBuffers(1, &terrain->m_BufferID);

  /* Delete chunks */
  if (terrain->m_Chunks)
    for (x = 0; x < terrain->m_ChunksX; x++)
    {
      /* For each chunk at row */
      if (terrain->m_Chunks[x])
        for (z = 0; z < terrain->m_ChunksZ; z++)
        {
          /* For each LOD level */
          if (terrain->m_Chunks[x][z].m_LODs)
          {
            for (levelLOD = 0; levelLOD < VE_TERRAIN_NUMLOD; levelLOD++)
            {
              /* For each trimming type */
              if (terrain->m_Chunks[x][z].m_LODs[levelLOD])
                for (trimType = 0; trimType < VE_TERRAIN_NUMTRIMS; trimType++)
                {
                  /* Delete index buffer */
                  if (terrain->m_Chunks[x][z].m_LODs[levelLOD][trimType].m_BufferID)
                    glDeleteBuffers(1, &terrain->m_Chunks[x][z].m_LODs[levelLOD][trimType].m_BufferID);

                    /* Delete faces array */
                    Delete(terrain->m_Chunks[x][z].m_LODs[levelLOD][trimType].m_Faces);
                }

              /* Delete LOD level row */
              Delete(terrain->m_Chunks[x][z].m_LODs[levelLOD]);
            }

            /* Delete LODs */
            Delete(terrain->m_Chunks[x][z].m_LODs);
          }
        }

      /* Delete chunks row */
      Delete(terrain->m_Chunks[x]);
    }

  /* Delete chunks array */
  Delete(terrain->m_Chunks);

  /* Delete terrain textures */
  VETextureDelete(terrain->m_Texture.m_MapBlend);
  VETextureDelete(terrain->m_Texture.m_Map1);
  VETextureDelete(terrain->m_Texture.m_Map2);
  VETextureDelete(terrain->m_Texture.m_Map3);
  VETextureDelete(terrain->m_Texture.m_Map4);

  /* Unload height map */
  VETextureUnload(terrain->m_HeightMap);

  /* Delete vertices */
  Delete(terrain->m_Vertices);

  /* Terrain deletion */
  Delete(terrain);
} /* End of 'VETerrainDeleteInternal' function */

/***
 * PURPOSE: Unload terrain from scene
 *   PARAM: [IN] sceneID - scene identifier to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainUnload( const VEUINT sceneID )
{
  VESCENE *scene = NULL;

  /* Enter critical section */
  VESectionEnterInternal(p_SettingsGraphicsSection);

  /* Obtain scene */
  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Delete terrain */
  VETerrainDeleteInternal(scene->m_Terrain);
  scene->m_Terrain = NULL;

  /* That's it */
  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VETerrainUnload' function */

/***
 * PURPOSE: Update terrain
 *   PARAM: [IN] terrain - pointer to terrain to update
 *   PARAM: [IN] camera  - pointer to currenct camera
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainUpdateInternal( VETERRAIN *terrain, const VECAMERA *camera )
{
  VEINT chunkX = 0, chunkZ = 0;
  VEINT distanceX = 0, distanceZ = 0, distance = 0;

  /* Determine current chunk */
  terrain->m_ChunkX = camera->m_Position.m_X / (VE_TERRAIN_CHUNKSIZE * terrain->m_ScaleFactor.m_X);
  terrain->m_ChunkZ = camera->m_Position.m_Z / (VE_TERRAIN_CHUNKSIZE * terrain->m_ScaleFactor.m_Z);

  /* Update visibility & LOD */
  for (chunkX = 0; chunkX < terrain->m_ChunksX; chunkX++)
    for (chunkZ = 0; chunkZ < terrain->m_ChunksZ; chunkZ++)
    {
      /* Determine distances to current chunk from camera's chunk */
      distanceX = abs(terrain->m_ChunkX - chunkX);
      distanceZ = abs(terrain->m_ChunkZ - chunkZ);
      distance = VEMAX(distanceX, distanceZ) - 1;
      if (distance < 0)
        distance = 0;
      terrain->m_Chunks[chunkX][chunkZ].m_LevelLOD   = VEMIN(distance, VE_TERRAIN_NUMLOD-1);
      terrain->m_Chunks[chunkX][chunkZ].m_IsVisisble = VEIsAABBInFrustum(terrain->m_Chunks[chunkX][chunkZ].m_AABB);
      terrain->m_Chunks[chunkX][chunkZ].m_TrimType   = VE_TERRAIN_NOTRIM;
    }

  /* Update trimming */
  for (chunkX = 0; chunkX < terrain->m_ChunksX; chunkX++)
    for (chunkZ = 0; chunkZ < terrain->m_ChunksZ; chunkZ++)
    {
      VETERRAINCHUNK *chunk = &terrain->m_Chunks[chunkX][chunkZ];

      /******** X to left, Z to up
       *     1   2   3   4   5
       *   |-------------------|
       * A | 1 | 1 | 1 | 1 | 1 |
       *   |-------------------|
       * B | 1 | 0 | 0 | 0 | 1 |
       *   |-------------------|
       * C | 1 | 0 | X | 0 | 1 |
       *   |-------------------|
       * D | 1 | 0 | 0 | 0 | 1 |
       *   |-------------------|
       * E | 1 | 1 | 1 | 1 | 1 |
       *   |-------------------|
       ********/

      /* Trimming B2 - B4 chunks */
      if ((chunkZ < terrain->m_ChunksZ-1)&&(terrain->m_Chunks[chunkX][chunkZ+1].m_LevelLOD > chunk->m_LevelLOD))
        chunk->m_TrimType = VE_TERRAIN_TRIMBOTTOM;

      /* Trimming D2 - D4 chunks */
      if ((chunkZ > 0)&&(terrain->m_Chunks[chunkX][chunkZ-1].m_LevelLOD > chunk->m_LevelLOD))
        chunk->m_TrimType = VE_TERRAIN_TRIMTOP;

      /* Trimming B2 - D2 */
      if ((chunkX < terrain->m_ChunksX-1)&&(terrain->m_Chunks[chunkX+1][chunkZ].m_LevelLOD > chunk->m_LevelLOD))
        chunk->m_TrimType = VE_TERRAIN_TRIMRIGHT;

      /* Trimming B4 - D4 */
      if ((chunkX > 0)&&(terrain->m_Chunks[chunkX-1][chunkZ].m_LevelLOD > chunk->m_LevelLOD))
        chunk->m_TrimType = VE_TERRAIN_TRIMLEFT;

      /* Trimming D4 */
      if (((chunkX > 0)&&(terrain->m_Chunks[chunkX-1][chunkZ].m_LevelLOD > chunk->m_LevelLOD))&&((chunkZ > 0)&&(terrain->m_Chunks[chunkX][chunkZ-1].m_LevelLOD > chunk->m_LevelLOD)))
        chunk->m_TrimType = VE_TERRAIN_TRIMLEFTTOP;

      /* Trimming D2 */
      if (((chunkX < terrain->m_ChunksX-1)&&(terrain->m_Chunks[chunkX+1][chunkZ].m_LevelLOD > chunk->m_LevelLOD))&&((chunkZ > 0)&&(terrain->m_Chunks[chunkX][chunkZ-1].m_LevelLOD > chunk->m_LevelLOD)))
        chunk->m_TrimType = VE_TERRAIN_TRIMRIGHTTOP;

      /* Trimming B2 */
      if (((chunkX < terrain->m_ChunksX-1)&&(terrain->m_Chunks[chunkX+1][chunkZ].m_LevelLOD > chunk->m_LevelLOD))&&((chunkZ < terrain->m_ChunksZ-1)&&(terrain->m_Chunks[chunkX][chunkZ+1].m_LevelLOD > chunk->m_LevelLOD)))
        chunk->m_TrimType = VE_TERRAIN_TRIMRIGHTBOTTOM;

      /* Trimming B4 */
      if (((chunkX > 0)&&(terrain->m_Chunks[chunkX-1][chunkZ].m_LevelLOD > chunk->m_LevelLOD))&&((chunkZ < terrain->m_ChunksZ-1)&&(terrain->m_Chunks[chunkX][chunkZ+1].m_LevelLOD > chunk->m_LevelLOD)))
        chunk->m_TrimType = VE_TERRAIN_TRIMLEFTBOTTOM;
    }
} /* End of 'VETerrainUpdateInternal' function */

/***
 * PURPOSE: Bind attributes and use terrain shader program
 *   PARAM: [IN] terrain - pointer to terrain to render
 *   PARAM: [IN] chunkX  - chunk X number
 *   PARAM: [IN] chunkZ  - chunk Z number
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainChunkProgramUseInternal( const VETERRAIN *terrain, const VEUINT chunkX, const VEUINT chunkZ )
{
  if (!terrain)
    return;

  /* Select program */
  glUseProgram(terrain->m_ProgramID);
  if (!terrain->m_ProgramID)
    return;

  /* Blend map */
  if ((terrain->m_Shader.m_ShaderMapBlend != -1)&&(terrain->m_Texture.m_MapBlend != 0))
  {
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, terrain->m_Texture.m_MapBlend);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_EXT);
    glTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_RGB_EXT, GL_REPLACE);
    glUniform1i(terrain->m_Shader.m_ShaderMapBlend, 0);
  } else
  {
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, 0);
  }

  /* Map #1 */
  if ((terrain->m_Shader.m_ShaderMap1 != -1)&&(terrain->m_Texture.m_Map1 != 0))
  {
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, terrain->m_Texture.m_Map1);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_EXT);
    glTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_RGB_EXT, GL_REPLACE);
    glUniform1i(terrain->m_Shader.m_ShaderMap1, 1);
  } else
  {
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, 0);
  }

  /* Map #2 */
  if ((terrain->m_Shader.m_ShaderMap2 != -1)&&(terrain->m_Texture.m_Map2 != 0))
  {
    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, terrain->m_Texture.m_Map2);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_EXT);
    glTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_RGB_EXT, GL_REPLACE);
    glUniform1i(terrain->m_Shader.m_ShaderMap2, 2);
  } else
  {
    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, 0);
  }

  /* Map #3 */
  if ((terrain->m_Shader.m_ShaderMap3 != -1)&&(terrain->m_Texture.m_Map2 != 0))
  {
    glActiveTexture(GL_TEXTURE3);
    glBindTexture(GL_TEXTURE_2D, terrain->m_Texture.m_Map3);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_EXT);
    glTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_RGB_EXT, GL_REPLACE);
    glUniform1i(terrain->m_Shader.m_ShaderMap3, 3);
  } else
  {
    glActiveTexture(GL_TEXTURE3);
    glBindTexture(GL_TEXTURE_2D, 0);
  }

  /* Map #4 */
  if ((terrain->m_Shader.m_ShaderMap4 != -1)&&(terrain->m_Texture.m_Map4 != 0))
  {
    glActiveTexture(GL_TEXTURE4);
    glBindTexture(GL_TEXTURE_2D, terrain->m_Texture.m_Map4);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_EXT);
    glTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_RGB_EXT, GL_REPLACE);
    glUniform1i(terrain->m_Shader.m_ShaderMap4, 4);
  } else
  {
    glActiveTexture(GL_TEXTURE4);
    glBindTexture(GL_TEXTURE_2D, 0);
  }

  /* Setup blend texture size */
  glUniform2f(terrain->m_Shader.m_ShaderParams, terrain->m_Width, terrain->m_Height);
} /* End of 'VETerrainChunkProgramUseInternal' function */

/***
 * PURPOSE: Render terrain chunk
 *   PARAM: [IN] terrain - pointer to terrain to render
 *   PARAM: [IN] chunkX  - chunk X number
 *   PARAM: [IN] chunkZ  - chunk Z number
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainRenderChunk( VETERRAIN *terrain, const VEUINT chunkX, const VEUINT chunkZ )
{
  VETERRAINCHUNK *chunk  = &terrain->m_Chunks[chunkX][chunkZ];
  VETERRAINLOD *chunkLOD = &chunk->m_LODs[chunk->m_LevelLOD][chunk->m_TrimType];

  if (terrain->m_IsWired)
  {
    VEUINT faceID = 0;

    glBegin(GL_LINES);
    for (faceID = 0; faceID < chunkLOD->m_NumFaces; faceID++)
    {
      VEVERTEX *v1 = &terrain->m_Vertices[chunkLOD->m_Faces[faceID].m_V1];
      VEVERTEX *v2 = &terrain->m_Vertices[chunkLOD->m_Faces[faceID].m_V2];
      VEVERTEX *v3 = &terrain->m_Vertices[chunkLOD->m_Faces[faceID].m_V3];

      /* 1 -> 2 */
      glVertex3fv((const GLfloat*)&v1->m_Position);
      glNormal3fv((const GLfloat*)&v1->m_Normal);
      glColor3f(v1->m_Position.m_Y / 25.5, 1.0, v1->m_Position.m_Y / 25.5);

      glVertex3fv((const GLfloat*)&v2->m_Position);
      glNormal3fv((const GLfloat*)&v2->m_Normal);
      glColor3f(v2->m_Position.m_Y / 25.5, 1.0, v2->m_Position.m_Y / 25.5);

      /* 2 -> 3 */
      glVertex3fv((const GLfloat*)&v2->m_Position);
      glNormal3fv((const GLfloat*)&v2->m_Normal);
      glColor3f(v2->m_Position.m_Y / 25.5, 1.0, v2->m_Position.m_Y / 25.5);

      glVertex3fv((const GLfloat*)&v3->m_Position);
      glNormal3fv((const GLfloat*)&v3->m_Normal);
      glColor3f(v3->m_Position.m_Y / 25.5, 1.0, v3->m_Position.m_Y / 25.5);

      /* 3 -> 1 */
      glVertex3fv((const GLfloat*)&v3->m_Position);
      glNormal3fv((const GLfloat*)&v3->m_Normal);
      glColor3f(v3->m_Position.m_Y / 25.5, 1.0, v3->m_Position.m_Y / 25.5);

      glVertex3fv((const GLfloat*)&v1->m_Position);
      glNormal3fv((const GLfloat*)&v1->m_Normal);
      glColor3f(v1->m_Position.m_Y / 25.5, 1.0, v1->m_Position.m_Y / 25.5);
    }
    glEnd();
  } else
  {
    /* Enable terrain shader */
    VETerrainChunkProgramUseInternal(terrain, chunkX, chunkZ);

    glEnableClientState(GL_TEXTURE_COORD_ARRAY);
    glEnableClientState(GL_NORMAL_ARRAY);
    glEnableClientState(GL_VERTEX_ARRAY);

    /* Setup current vertices buffer */
    glBindBuffer(GL_ARRAY_BUFFER, terrain->m_BufferID);

    /* Enable blend texture */
    glBindTexture(GL_TEXTURE_2D, GL_TEXTURE0);

    /* Send pointers */
    glVertexPointer(3, GL_FLOAT, sizeof(VEVERTEX), BUFFER_OFFSET(0));
    glNormalPointer(GL_FLOAT, sizeof(VEVERTEX), BUFFER_OFFSET(12));
    glTexCoordPointer(2, GL_FLOAT, sizeof(VEVERTEX), BUFFER_OFFSET(24));
    //glColorPointer(3, GL_FLOAT, sizeof(VEVERTEX), BUFFER_OFFSET(32));

    glDrawElements(GL_TRIANGLES, 3 * chunkLOD->m_NumFaces, GL_UNSIGNED_INT, chunkLOD->m_Faces);

    /* Unbind buffers */
    glBindBuffer(GL_ARRAY_BUFFER,        0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    /* Disable rrays */
    glDisableClientState(GL_TEXTURE_COORD_ARRAY);
    glDisableClientState(GL_NORMAL_ARRAY);
    glDisableClientState(GL_VERTEX_ARRAY);

    /* Disable terrain shader */
    glUseProgram(0);
  }
} /* End of 'VETerrainRenderChunk' function */

/***
 * PURPOSE: Render terrain
 *   PARAM: [IN] terrain - pointer to terrain to render
 *   PARAM: [IN] camera  - pointer to currenct camera
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainRenderInternal( VETERRAIN *terrain, const VECAMERA *camera )
{
  VEUINT chunkX = 0, chunkZ = 0;

  if (!terrain)
    return;

  /* Update terrain state */
  VETerrainUpdateInternal(terrain, camera);

  /* Rendering terrain */
  for (chunkX = 0; chunkX < terrain->m_ChunksX; chunkX++)
    for (chunkZ = 0; chunkZ < terrain->m_ChunksZ; chunkZ++)
      if (terrain->m_Chunks[chunkX][chunkZ].m_IsVisisble)
        VETerrainRenderChunk(terrain, chunkX, chunkZ);
} /* End of 'VETerrainRenderInternal' function */

/***
 * PURPOSE: Set terrain rendering type
 *   PARAM: [IN] sceneID - scene identifier to render
 *   PARAM: [IN] isWired - wired rendering flag
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETerrainSetWired( const VEUINT sceneID, const VEBOOL isWired )
{
  VESCENE *scene = NULL;

  /* Enter critical section */
  VESectionEnterInternal(p_SettingsGraphicsSection);

  /* Obtain scene */
  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Set terrain rendering type */
  if (scene->m_Terrain)
    scene->m_Terrain->m_IsWired = isWired;

  /* That's it */
  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VETerrainSetWired' function */
