#ifndef TEXTUREMANAGER_H_INCLUDED
#define TEXTUREMANAGER_H_INCLUDED

#include "types.h"

/***
 * PURPOSE: Load texture from file
 *  RETURN: Texture ID if success, FALSE otherwise
 *   PARAM: [IN] fileName - texture file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VETextureLoad( const VEBUFFER fileName );

/***
 * PURPOSE: Delete loaded texture
 *   PARAM: [IN] textureID - loaded texture identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETextureDelete( const VEUINT textureID );

#endif // TEXTUREMANAGER_H_INCLUDED
