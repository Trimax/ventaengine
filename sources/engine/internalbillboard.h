#ifndef INTERNALBILLBOARD_H_INCLUDED
#define INTERNALBILLBOARD_H_INCLUDED

#include "internalshaders.h"
#include "types.h"

/* Billboard structure */
typedef struct tagVEBILLBOARD
{
  VEUINT           m_TextureID;        /* Texture identifier */
  VEVECTOR3D       m_Position;         /* Position */
  VEREAL           m_Size;             /* Billboard size */
  VESHADERINTERNAL m_ShaderParameters; /* Shader parameters */
} VEBILLBOARD;

/***
 * PURPOSE: Render billboard
 *   PARAM: [IN] billboard - pointer to billboard
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBillboardRender( VEBILLBOARD *billboard );

#endif // INTERNALBILLBOARD_H_INCLUDED
