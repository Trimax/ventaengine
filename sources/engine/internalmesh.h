#ifndef INTERNALMESH_H_INCLUDED
#define INTERNALMESH_H_INCLUDED

#include "internalboundsphere.h"
#include "internalmaterial.h"
#include "internalshaders.h"
#include "internalvertex.h"
#include "vector4d.h"
#include "color.h"

#include <stdio.h>

typedef struct tagVEMESH
{
  VEUINT            m_ProgramID;        /* Linked shader program, attached to mesh */
  VEUINT            m_BufferID;         /* Vertex buffer identifier */
  VEUINT            m_NumVertices;      /* Number of vertices */
  VEUINT            m_NumKeys;          /* Number of animation keys */
  VEVERTEX         *m_Vertices;         /* Vertices array */
  VEVECTOR3D       *m_Positions;        /* Animation positions */
  VEVECTOR4D       *m_Quaternions;      /* Animation quaternions */
  VEREAL           *m_QAngles;          /* Angles between quaternions */
  VEMATERIAL       *m_Material;         /* Pointer to material */
  VEBOUNDINGSPHERE  m_BoundingSphere;   /* Bounding sphere*/
  VESHADERINTERNAL  m_ShaderParameters; /* Shader parameters */
} VEMESH;

/***
 * PURPOSE: Render mesh
 *   PARAM: [IN] mesh - pointer to mesh to render (NOT NULL)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMeshRender( VEMESH *mesh );

/***
 * PURPOSE: Build per-vertex tangents
 *   PARAM: [IN]  mesh - pointer to mesh to render
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMeshBuildTangents( VEMESH *mesh );

/***
 * PURPOSE: Delete mesh data
 *   PARAM: [IN] mesh - pointer to mesh
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMeshDelete( VEMESH *mesh );

#endif // INTERNALMESH_H_INCLUDED
