#ifndef INTERNALHEADER_H_INCLUDED
#define INTERNALHEADER_H_INCLUDED

#include "types.h"

/* Current formats version */
#define VE_FORMATVERSION 1

/*** File formats ***/

/* Dialog format */
#define VE_FORMAT_DIALOG            1

/* Texture format */
#define VE_FORMAT_TEXTURE           2

/* Skin format */
#define VE_FORMAT_SKIN              3

/* 3D object format */
#define VE_FORMAT_OBJECT            4

/* Scene format */
#define VE_FORMAT_SCENE             5

/* Sound file format */
#define VE_FORMAT_SOUND             6

/* Resource file format */
#define VE_FORMAT_RESOURCE          7

/* Compressed texture file format */
#define VE_FORMAT_TEXTURECOMPRESSED 8

/* Terrain file format */
#define VE_FORMAT_TERRAIN           9

/* Common engine file header */
typedef struct tagVEHEADER
{
  VEBYTE m_Prefix;  /* Common VE preffix */
  VEBYTE m_Format;  /* File format (one of VE_FORMAT_XXX definitions) */
  VEUINT m_Version; /* Format version */
} VEHEADER;

/***
 * PURPOSE: Check if header is valid
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] header     - header to validate
 *   PARAM: [IN] fileFormat - expected file format (see VE_FORMAT_XXX definitions)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEHeaderIsValid( const VEHEADER header, const VEBYTE fileFormat );

#endif // INTERNALHEADER_H_INCLUDED
