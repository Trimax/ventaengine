#ifndef INTERNALMATERIAL_H_INCLUDED
#define INTERNALMATERIAL_H_INCLUDED

#include "types.h"
#include "color.h"

#include <stdio.h>

/* Material structure */
typedef struct tagVEMATERIAL
{
  VECOLOR   m_Ambient;          /* Ambient color part */
  VECOLOR   m_Diffuse;          /* Diffuse color part */
  VECOLOR   m_Specular;         /* Specular color part */
  VECOLOR   m_SelfIllumination; /* Self-illumination color */

  VEREAL    m_SpecularLevel;    /* Specular level */
  VEREAL    m_Glossiness;       /* Level of glossiness */

  VEUINT    m_MapDiffuse;       /* OpenGL diffuse texture identifier */
  VEUINT    m_MapOpacity;       /* OpenGL opacity texture identifier */
  VEUINT    m_MapNormals;       /* OpenGL normals texture identifier */
  VEUINT    m_MapReflection;    /* OpenGL reflection texture identifier */

  VEUINT    m_MaterialID;       /* 3DsMAX material identifier */
} VEMATERIAL;

/***
 * PURPOSE: Load material
 *  RETURN: Pointer to loaded material data if success, NULL otherwise
 *   PARAM: [IN] f - pointer to file to read data
 *  AUTHOR: Eliseev Dmitry
 ***/
VEMATERIAL *VEMaterialLoad( FILE *f );

#endif // INTERNALMATERIAL_H_INCLUDED
