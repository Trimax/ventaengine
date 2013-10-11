#include "internalglut.h"

#include "internalresourcemanager.h"
#include "internaltexturemanager.h"
#include "internalcontainer.h"
#include "texturemanager.h"
#include "internalstring.h"
#include "internalbuffer.h"
#include "memorymanager.h"

#include <string.h>
#include <stdlib.h>
#include <stdio.h>

/* Texture information */
typedef struct tagVETEXTUREINFO
{
  VESTRING *m_Name; /* Texture file name */
  VEUINT    m_ID;   /* Texture OpenGL identifier */
} VETEXTUREINFO;

/* Textures container */
volatile static VEINTERNALCONTAINER *p_Textures = NULL;

/***
 * PURPOSE: Try to find the same texture at manager
 *  RETURN: Found texture ID if success, 0 otherwise
 *   PARAM: [IN] textureName - texture file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VETextureGetByName( const VEBUFFER textureName )
{
  VEUINT textureID = 0, pos = 0;
  if (!p_Textures)
    return 0;

  VESectionEnterInternal(p_Textures->m_Sync);

  for (pos = 0; (pos < p_Textures->m_Size)&&(textureID == 0); pos++)
    if (p_Textures->m_Items[pos])
    {
      VETEXTUREINFO *texture = (VETEXTUREINFO*)p_Textures->m_Items[pos];
      if (VEBuffersEqual(texture->m_Name->m_Data, textureName))
        textureID = texture->m_ID;
    }

  VESectionLeaveInternal(p_Textures->m_Sync);

  return textureID;
} /* End of 'VETextureGetByName' function */

/***
 * PURPOSE: Initialize texture manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETextureInit( VEVOID )
{
  /* Manager already initialized */
  if (p_Textures)
    return TRUE;

  /* Creation of textures container */
  p_Textures = VEInternalContainerCreate(VE_DEFAULT_CONTAINERSIZE, "Textures manager");
  if (!p_Textures)
    return FALSE;

  /* That's it */
  return TRUE;
} /* End of 'VETextureInit' function */

/***
 * PURPOSE: Delete texture internal
 *   PARAM: [IN] textureInfo - pointer to texture to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETextureDeleteInternal( VETEXTUREINFO *textureInfo )
{
  if (!textureInfo)
    return;

  /* Release video memory */
  glDeleteTextures(1, &textureInfo->m_ID);
  VEStringDeleteInternal(textureInfo->m_Name);
  Delete(textureInfo);
} /* End of 'VETextureDeleteInternal' function */

/***
 * PURPOSE: Deinitialize texture manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETextureDeinit( VEVOID )
{
  if (!p_Textures)
    return;

  /* Remove textures container */
  VEInternalContainerForeach((VEINTERNALCONTAINER*)p_Textures, (VEFUNCTION)VETextureDeleteInternal);
  VEInternalContainerDelete((VEINTERNALCONTAINER*)p_Textures);
  p_Textures = NULL;
} /* End of 'VETextureDeinit' function */

/***
 * PURPOSE: Creates texture using data
 *  RETURN: Texture ID if success, FALSE otherwise
 *   PARAM: [IN] texture  - pointer to data
 *   PARAM: [IN] filename - texture file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VETextureCreate( const VETEXTURE *texture, const VEBUFFER filename )
{
  VEUINT nameLength = VEBufferLength(filename);
  VETEXTUREINFO *textureInfo = New(sizeof(VETEXTUREINFO), "Texture info");
  if (!textureInfo)
    return 0;

  /* Allocate memory for texture name */
  textureInfo->m_Name = VEStringCreateInternal(nameLength + 1);
  if (!textureInfo->m_Name)
  {
    Delete(textureInfo);
    return 0;
  }

  /* Store texture name */
  textureInfo->m_Name->m_Length = nameLength;
  memcpy(textureInfo->m_Name->m_Data, filename, nameLength);

  /* Enable textures support */
  glEnable(GL_TEXTURE_2D);

  /* Prepare video memory for one texture */
  glGenTextures(1, &textureInfo->m_ID);

  /* Select current texture buffer */
  glBindTexture(GL_TEXTURE_2D, textureInfo->m_ID);

  /* Copy data to video memory */
  glTexImage2D(GL_TEXTURE_2D, 0, 3, texture->m_Width, texture->m_Height, 0, GL_RGBA, GL_UNSIGNED_BYTE, texture->m_Data);

  /* Setup filtration parameters */
  glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
  glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
  gluBuild2DMipmaps(GL_TEXTURE_2D,4,texture->m_Width,texture->m_Height,GL_RGBA,GL_UNSIGNED_BYTE,texture->m_Data);

  /* Unbind texture */
  glBindTexture(GL_TEXTURE_2D, 0);

  /* Disable textures support */
  glDisable(GL_TEXTURE_2D);

  /* Add texture to */
  return VEInternalContainerAdd((VEINTERNALCONTAINER*)p_Textures, textureInfo);
} /* End of 'VETextureCreate' function */

/***
 * PURPOSE: Load texture from file
 *  RETURN: Pointer to allocated texture
 *   PARAM: [IN] fileName - texture file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VETEXTURE *VETextureLoadInternal( const VEBUFFER fileName )
{
  VETEXTURE *texture = NULL;
  VEFILE *f = NULL;

  /* Try to open file */
  f = VEResourcePtrOpen(fileName);
  if (!f)
    return NULL;

  /* Determine texture format */
  if (VEBufferIsEndsWith(f->m_Name->m_Data, ".tga"))
    texture = VETextureLoadTGA(f->m_Ptr);
  else if (VEBufferIsEndsWith(f->m_Name->m_Data, ".vet"))
    texture = VETextureLoadVET(f->m_Ptr);

  /* Close file */
  VEResourcePtrClose(f);
  return texture;
} /* End of 'VETextureLoadInternal' function */

/***
 * PURPOSE: Load texture from file
 *  RETURN: Texture ID if success, FALSE otherwise
 *   PARAM: [IN] fileName - texture file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VETextureLoad( const VEBUFFER fileName )
{
  VEUINT textureID = 0;
  VETEXTURE *texture = NULL;

  /* Textures manager is not initialized */
  if (!p_Textures)
    return 0;

  textureID = VETextureGetByName(fileName);
  if (textureID != 0)
    return textureID;

  /* Try to load texture */
  texture = VETextureLoadInternal(fileName);

  /* Wrong texture */
  if (!texture)
    return 0;

  /* Generate OpenGL texture */
  textureID = VETextureCreate(texture, fileName);

  /* Unload temporary data */
  VETextureUnload(texture);

  /* That's it */
  return textureID;
} /* End of 'VETextureLoad' function */

/***
 * PURPOSE: Delete loaded texture
 *   PARAM: [IN] textureID - loaded texture identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETextureDelete( const VEUINT textureID )
{
  VETEXTUREINFO *textureInfo = VEInternalContainerGet((VEINTERNALCONTAINER*)p_Textures, textureID);
  if (!textureInfo)
    return;

  /* Remove texture */
  VEInternalContainerRemove((VEINTERNALCONTAINER*)p_Textures, textureID);
  VETextureDeleteInternal(textureInfo);
} /* End of 'VETextureDelete' function */
