#ifndef INTERNALGEOMETRY_H_INCLUDED
#define INTERNALGEOMETRY_H_INCLUDED

#include "internalobject.h"
#include "internalmesh.h"

#include "vector3d.h"
#include "vector2d.h"
#include "types.h"
#include "color.h"

#include <stdio.h>

#define BUFFER_OFFSET(i) ((char *)NULL + (i))

/* Face definition */
typedef struct tagVEFACE
{
  VEUINT     m_Vertex1; /* Face vertex #1 */
  VEUINT     m_Vertex2; /* Face vertex #2 */
  VEUINT     m_Vertex3; /* Face vertex #3 */

  VEUINT     m_TVertex1; /* Face texture vertex #1 */
  VEUINT     m_TVertex2; /* Face texture vertex #2 */
  VEUINT     m_TVertex3; /* Face texture vertex #3 */

  VEVECTOR3D m_Normal;   /* Face normal */
  VEVECTOR3D m_Center;   /* Face center */
} VEFACE;

/* Texture coordinates definition */
typedef VEVECTOR2D VETCOORDINATES;

/* VEO format vertex */
typedef struct tagVEOVERTEX
{
  VEVECTOR3D m_Position;
  VEVECTOR3D m_Normal;
  VECOLOR    m_Color;
} VEOVERTEX;

/* Geometry definition */
typedef struct tagVEGEOMETRY
{
  VEUINT m_NumVertices;
  VEUINT m_NumFaces;
  VEUINT m_NumTVertices;

  VEINT  m_MaterialID;

  VEOVERTEX      *m_Vertices;
  VEFACE         *m_Faces;
  VETCOORDINATES *m_TCoordinates;

  VEUINT      m_NumKeys;      /* The number of animation keys */
  VEVECTOR3D *m_Positions;    /* Animation positions */
  VEVECTOR4D *m_Quaternions;  /* Animation quaternions */
} VEGEOMETRY;

/***
 * PURPOSE: Load geometry from file
 *  RETURN: Pointer to created geometry if success, NULL otherwise
 *   PARAM: [IN] f       - pointer to opened file
 *   PARAM: [IN] numKeys - the number of animation keys
 *  AUTHOR: Eliseev Dmitry
 ***/
VEGEOMETRY *VEGeometryLoad( FILE *f, const VEUINT numKeys );

/***
 * PURPOSE: Create cube geometry
 *  RETURN: Pointer to created geometry if success, NULL otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEGEOMETRY *VEGeometryCreateCube( VEVOID );

/***
 * PURPOSE: Build per-vertex normals
 *   PARAM: [IN/OUT] geom - geometry data to build normals
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGeometryBuildNormals( VEGEOMETRY *geom );

/***
 * PURPOSE: Transform object's geometry to renderable mesh
 *  RETURN: Pointer to created mesh if success, NULL otherwise
 *   PARAM: [IN] geom - pointer to created (readed) object's geometry
 *   PARAM: [IN] obj  - pointer to object (can be NULL)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEMESH *VEGeometryToMesh( const VEGEOMETRY *geom, const VEOBJECT *obj );

/***
 * PURPOSE: Delete geometry data
 *   PARAM: [IN] geometry - pointer to data to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGeometryDelete( VEGEOMETRY *geometry );

#endif // INTERNALGEOMETRY_H_INCLUDED
