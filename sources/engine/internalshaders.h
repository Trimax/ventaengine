#ifndef INTERNALSHADERS_H_INCLUDED
#define INTERNALSHADERS_H_INCLUDED

#include "types.h"

/* Shader parameters */
typedef struct tagVESHADERINTERNAL
{
  /* Maps */
  VEINT m_MapDiffuse;        /* Diffuse map: RGBA */
  VEINT m_MapNormals;        /* Normals map: XYZS (where S - shininess) */
  VEINT m_MapReflection;     /* Reflection maps */
  VEINT m_MapOpacity;        /* Opacity maps */

  VEINT m_MapDiffuseFlag;    /* Diffuse map flag */
  VEINT m_MapNormalsFlag;    /* Normals map flag */
  VEINT m_MapReflectionFlag; /* Reflection map flag */
  VEINT m_MapOpacityFlag;    /* Opacity map flag */

  VEINT m_CameraPosition;    /* Camera position */

  VEINT m_Lights;            /* Lights state */

  VEINT m_SunPosition;       /* Sun position variable */
  VEINT m_SkyColor;          /* Sky color */
} VESHADERINTERNAL;

/*** Default rendering ***/

/* Vertex shader */
extern VECHAR p_ShaderRenderVertex[];

/* Fragment shader */
extern VECHAR p_ShaderRenderFragment[];


/*** Normal mapping ***/

/* Vertex shader */
extern VECHAR p_ShaderNormalMappingVertex[];

/* Fragment shader */
extern VECHAR p_ShaderNormalMappingFragment[];

/*** Planet shading ***/

/* Vertex shader */
extern VECHAR p_ShaderPlanetVertex[];

/* Fragment shader */
extern VECHAR p_ShaderPlanetFragment[];

/*** Terraing shading ***/

/* Vertex shader */
extern VECHAR p_ShaderTerrainVertex[];

/* Fragment shader */
extern VECHAR p_ShaderTerrainFragment[];

/*** Cartoon shading ***/

/* Vertex shader */
extern VECHAR p_ShaderCartoonVertex[];

/* Fragment shader */
extern VECHAR p_ShaderCartoonFragment[];

/*** Environment shading ***/

/* Vertex shader */
extern VECHAR p_ShaderEnvironmentVertex[];

/* Fragment shader */
extern VECHAR p_ShaderEnvironmentFragment[];

/*** Billboard shading ***/

/* Vertex shader */
extern VECHAR p_ShaderBillboardVertex[];

/* Fragment shader */
extern VECHAR p_ShaderBillboardFragment[];

#endif // INTERNALSHADERS_H_INCLUDED
