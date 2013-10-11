#ifndef INTERNALTEXTUREMANAGER_H_INCLUDED
#define INTERNALTEXTUREMANAGER_H_INCLUDED

#include "types.h"

#include <stdio.h>

/* Texture data */
typedef struct tagVETEXTURE
{
  VEUINT  m_Width;  /* Width */
  VEUINT  m_Height; /* Height */
  VEBYTE *m_Data;   /* 32 bits data */
} VETEXTURE;

/***
 * PURPOSE: Initialize texture manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETextureInit( VEVOID );

/***
 * PURPOSE: Deinitialize texture manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETextureDeinit( VEVOID );

/***
 * PURPOSE: Load texture from TGA format
 *  RETURN: Pointer to loaded texture data if success, NULL otherwise
 *   PARAM: [IN] f - pointer to file to read data
 *  AUTHOR: Eliseev Dmitry
 ***/
VETEXTURE *VETextureLoadTGA( FILE *f );

/***
 * PURPOSE: Load texture from VET format
 *  RETURN: Pointer to loaded texture data if success, NULL otherwise
 *   PARAM: [IN] f - pointer to file to read data
 *  AUTHOR: Eliseev Dmitry
 ***/
VETEXTURE *VETextureLoadVET( FILE *f );

/***
 * PURPOSE: Load texture from file
 *  RETURN: Pointer to allocated texture
 *   PARAM: [IN] fileName - texture file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VETEXTURE *VETextureLoadInternal( const VEBUFFER fileName );

/***
 * PURPOSE: Save texture to VET format
 *   PARAM: [IN] filename   - file to store texture
 *   PARAM: [IN] texture    - pointer to loaded texture
 *   PARAM: [IN] compressed - flag to compress texture
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETextureSave( const VEBUFFER filename, const VETEXTURE *texture, const VEBOOL compressed );

/***
 * PURPOSE: Unload texture
 *   PARAM: [IN] texture - pointer to loaded texture
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETextureUnload( VETEXTURE *texture );

#endif // INTERNALTEXTUREMANAGER_H_INCLUDED
