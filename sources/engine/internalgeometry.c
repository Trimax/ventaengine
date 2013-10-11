#include "internalglut.h"

#include "internalgeometry.h"
#include "shadermanager.h"
#include "memorymanager.h"

#include <string.h>

/***
 * PURPOSE: Build per-vertex normals
 *   PARAM: [IN/OUT] geom - geometry data to build normals
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGeometryBuildNormals( VEGEOMETRY *geom )
{
  VEUINT index = 0;

  /* Wrong pointer */
  if (!geom)
    return;

  /* Reset previous normals */
  for (index = 0; index < geom->m_NumVertices; index++)
    geom->m_Vertices[index].m_Normal = VEVector3D(0.0, 0.0, 0.0);

  /* Accumulate normal */
  for (index = 0; index < geom->m_NumFaces; index++)
  {
    VEVECTOR3D normal = geom->m_Faces[index].m_Normal;
    VEUINT v1 = geom->m_Faces[index].m_Vertex1;
    VEUINT v2 = geom->m_Faces[index].m_Vertex2;
    VEUINT v3 = geom->m_Faces[index].m_Vertex3;

    geom->m_Vertices[v1].m_Normal = VEVector3DAdd(geom->m_Vertices[v1].m_Normal, normal);
    geom->m_Vertices[v2].m_Normal = VEVector3DAdd(geom->m_Vertices[v2].m_Normal, normal);
    geom->m_Vertices[v3].m_Normal = VEVector3DAdd(geom->m_Vertices[v3].m_Normal, normal);
  }

  /* Normalization */
  for (index = 0; index < geom->m_NumVertices; index++)
    geom->m_Vertices[index].m_Normal = VEVector3DNormalize(geom->m_Vertices[index].m_Normal);
} /* End of 'VEGeometryBuildNormals' function */

/***
 * PURPOSE: Load geometry from file
 *  RETURN: Pointer to created geometry if success, NULL otherwise
 *   PARAM: [IN] f       - pointer to opened file
 *   PARAM: [IN] numKeys - the number of animation keys
 *  AUTHOR: Eliseev Dmitry
 ***/
VEGEOMETRY *VEGeometryLoad( FILE *f, const VEUINT numKeys )
{
  VEUINT id = 0;

  VEGEOMETRY *geometry = New(sizeof(VEGEOMETRY), "Object's geometry");
  if (!geometry)
    return NULL;

  /* Reading geometry header */
  fread(&geometry->m_NumVertices, 1, sizeof(VEUINT), f);
  fread(&geometry->m_NumFaces, 1, sizeof(VEUINT), f);
  fread(&geometry->m_NumTVertices, 1, sizeof(VEUINT), f);
  fread(&geometry->m_MaterialID, 1, sizeof(VEINT), f);

  /* Allocation of faces array */
  geometry->m_Faces = New(sizeof(VEFACE) * geometry->m_NumFaces, "Object's faces");
  if (!geometry->m_Faces)
  {
    VEGeometryDelete(geometry);
    return NULL;
  }

  /* Allocation of vertices array */
  geometry->m_Vertices = New(sizeof(VEOVERTEX) * geometry->m_NumVertices, "Object's vertices");
  if (!geometry->m_Vertices)
  {
    VEGeometryDelete(geometry);
    return NULL;
  }

  /* Allocation of texture coordinates array */
  geometry->m_TCoordinates = New(sizeof(VETCOORDINATES) * geometry->m_NumTVertices, "Object's texture vertices ");
  if (!geometry->m_TCoordinates)
  {
    VEGeometryDelete(geometry);
    return NULL;
  }

  /* Reading vertices */
  for (id = 0; id < geometry->m_NumVertices; id++)
  {
    fread(&geometry->m_Vertices[id].m_Position, 1, sizeof(VEVECTOR3D), f);
    fread(&geometry->m_Vertices[id].m_Normal,   1, sizeof(VEVECTOR3D), f);
    geometry->m_Vertices[id].m_Color = VECOLOR_WHITE;
  }

  /* Reading texture coordinates */
  for (id = 0; id < geometry->m_NumTVertices; id++)
    fread(&geometry->m_TCoordinates[id], 1, sizeof(VETCOORDINATES), f);

  /* Reading faces */
  for (id = 0; id < geometry->m_NumFaces; id++)
    fread(&geometry->m_Faces[id], 1, sizeof(VEFACE), f);

  /* Storing number of animation keys */
  geometry->m_NumKeys = numKeys;

  /* Memory allocation for animation positions */
  geometry->m_Positions = New(sizeof(VEVECTOR3D) * numKeys, "Geometry positions");
  if (!geometry->m_Positions)
  {
    VEGeometryDelete(geometry);
    return NULL;
  }

  /* Memory allocation for animation quaternions */
  geometry->m_Quaternions = New(sizeof(VEVECTOR4D) * numKeys, "Geometry quaternions");
  if (!geometry->m_Quaternions)
  {
    VEGeometryDelete(geometry);
    return NULL;
  }

  /* Reading animation */
  for (id = 0; id < numKeys; id++)
  {
    fread(&geometry->m_Positions[id],   1, sizeof(VEVECTOR3D), f);
    fread(&geometry->m_Quaternions[id], 1, sizeof(VEVECTOR4D), f);
  }

  /* Reading */
  return geometry;
} /* End of 'VEGraphicsGeometryLoad' function */

/***
 * PURPOSE: Create cube geometry
 *  RETURN: Pointer to created geometry if success, NULL otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEGEOMETRY *VEGeometryCreateCube( VEVOID )
{
  VEGEOMETRY *cube = New(sizeof(VEGEOMETRY), "Geometry data");
  if (!cube)
    return NULL;

  /* Cube has 12 polygons & 8 vertices */
  cube->m_NumFaces     = 12;
  cube->m_NumVertices  = 8;
  cube->m_NumTVertices = 4;

  /* Vertices array */
  cube->m_Vertices = New(sizeof(VEOVERTEX) * cube->m_NumVertices, "Cube geometry vertices");
  if (!cube->m_Vertices)
  {
    Delete(cube);
    return NULL;
  }

  /* Faces array */
  cube->m_Faces = New(sizeof(VEFACE) * cube->m_NumFaces, "Cube geometry faces");
  if (!cube->m_Faces)
  {
    Delete(cube->m_Vertices);
    Delete(cube);
    return NULL;
  }

  /* Texture coordinates array */
  cube->m_TCoordinates = New(sizeof(VETCOORDINATES) * cube->m_NumTVertices, "Cube geometry texture vertices");
  if (!cube->m_Faces)
  {
    Delete(cube->m_Faces);
    Delete(cube->m_Vertices);
    Delete(cube);
    return NULL;
  }

  /* Fill texture coordinates */
  cube->m_TCoordinates[0].m_X = 0.0;
  cube->m_TCoordinates[0].m_Y = 0.0;

  cube->m_TCoordinates[1].m_X = 1.0;
  cube->m_TCoordinates[1].m_Y = 0.0;

  cube->m_TCoordinates[2].m_X = 1.0;
  cube->m_TCoordinates[2].m_Y = 1.0;

  cube->m_TCoordinates[3].m_X = 0.0;
  cube->m_TCoordinates[3].m_Y = 1.0;

  /* Fill vertices */
  cube->m_Vertices[0].m_Position.m_X = 0.0;
  cube->m_Vertices[0].m_Position.m_Y = 0.0;
  cube->m_Vertices[0].m_Position.m_Z = 0.0;

  cube->m_Vertices[0].m_Normal.m_X   = -0.3;
  cube->m_Vertices[0].m_Normal.m_Y   = -0.3;
  cube->m_Vertices[0].m_Normal.m_Z   = -0.3;

  cube->m_Vertices[0].m_Color = VECOLOR_WHITE;

  cube->m_Vertices[1].m_Position.m_X = 0.0;
  cube->m_Vertices[1].m_Position.m_Y = 1.0;
  cube->m_Vertices[1].m_Position.m_Z = 0.0;

  cube->m_Vertices[1].m_Normal.m_X   = -0.3;
  cube->m_Vertices[1].m_Normal.m_Y   = 0.3;
  cube->m_Vertices[1].m_Normal.m_Z   = -0.3;

  cube->m_Vertices[1].m_Color = VECOLOR_WHITE;

  cube->m_Vertices[2].m_Position.m_X = 1.0;
  cube->m_Vertices[2].m_Position.m_Y = 1.0;
  cube->m_Vertices[2].m_Position.m_Z = 0.0;

  cube->m_Vertices[2].m_Normal.m_X   = 0.3;
  cube->m_Vertices[2].m_Normal.m_Y   = 0.3;
  cube->m_Vertices[2].m_Normal.m_Z   = -0.3;

  cube->m_Vertices[2].m_Color = VECOLOR_WHITE;

  cube->m_Vertices[3].m_Position.m_X = 1.0;
  cube->m_Vertices[3].m_Position.m_Y = 0.0;
  cube->m_Vertices[3].m_Position.m_Z = 0.0;

  cube->m_Vertices[3].m_Normal.m_X   = 0.3;
  cube->m_Vertices[3].m_Normal.m_Y   = -0.3;
  cube->m_Vertices[3].m_Normal.m_Z   = -0.3;

  cube->m_Vertices[3].m_Color = VECOLOR_WHITE;

  cube->m_Vertices[4].m_Position.m_X = 0.0;
  cube->m_Vertices[4].m_Position.m_Y = 0.0;
  cube->m_Vertices[4].m_Position.m_Z = 1.0;

  cube->m_Vertices[4].m_Normal.m_X   = -0.3;
  cube->m_Vertices[4].m_Normal.m_Y   = -0.3;
  cube->m_Vertices[4].m_Normal.m_Z   = 0.3;

  cube->m_Vertices[4].m_Color = VECOLOR_WHITE;

  cube->m_Vertices[5].m_Position.m_X = 0.0;
  cube->m_Vertices[5].m_Position.m_Y = 1.0;
  cube->m_Vertices[5].m_Position.m_Z = 1.0;

  cube->m_Vertices[5].m_Normal.m_X   = -0.3;
  cube->m_Vertices[5].m_Normal.m_Y   = 0.3;
  cube->m_Vertices[5].m_Normal.m_Z   = 0.3;

  cube->m_Vertices[5].m_Color = VECOLOR_WHITE;

  cube->m_Vertices[6].m_Position.m_X = 1.0;
  cube->m_Vertices[6].m_Position.m_Y = 1.0;
  cube->m_Vertices[6].m_Position.m_Z = 1.0;

  cube->m_Vertices[6].m_Normal.m_X   = 0.3;
  cube->m_Vertices[6].m_Normal.m_Y   = 0.3;
  cube->m_Vertices[6].m_Normal.m_Z   = 0.3;

  cube->m_Vertices[6].m_Color = VECOLOR_WHITE;

  cube->m_Vertices[7].m_Position.m_X = 1.0;
  cube->m_Vertices[7].m_Position.m_Y = 0.0;
  cube->m_Vertices[7].m_Position.m_Z = 1.0;

  cube->m_Vertices[7].m_Normal.m_X   = 0.3;
  cube->m_Vertices[7].m_Normal.m_Y   = -1.0;
  cube->m_Vertices[7].m_Normal.m_Z   = 0.3;

  cube->m_Vertices[7].m_Color = VECOLOR_WHITE;

  /* Fill faces */
  cube->m_Faces[0].m_Vertex1 = 0;
  cube->m_Faces[0].m_Vertex2 = 1;
  cube->m_Faces[0].m_Vertex3 = 3;

  cube->m_Faces[0].m_TVertex1 = 0;
  cube->m_Faces[0].m_TVertex2 = 1;
  cube->m_Faces[0].m_TVertex3 = 3;

  cube->m_Faces[1].m_Vertex1 = 1;
  cube->m_Faces[1].m_Vertex2 = 2;
  cube->m_Faces[1].m_Vertex3 = 3;

  cube->m_Faces[1].m_TVertex1 = 1;
  cube->m_Faces[1].m_TVertex2 = 2;
  cube->m_Faces[1].m_TVertex3 = 3;

  cube->m_Faces[2].m_Vertex1 = 2;
  cube->m_Faces[2].m_Vertex2 = 1;
  cube->m_Faces[2].m_Vertex3 = 6;

  cube->m_Faces[2].m_TVertex1 = 0;
  cube->m_Faces[2].m_TVertex2 = 1;
  cube->m_Faces[2].m_TVertex3 = 3;

  cube->m_Faces[3].m_Vertex1 = 1;
  cube->m_Faces[3].m_Vertex2 = 5;
  cube->m_Faces[3].m_Vertex3 = 6;

  cube->m_Faces[3].m_TVertex1 = 1;
  cube->m_Faces[3].m_TVertex2 = 2;
  cube->m_Faces[3].m_TVertex3 = 3;

  cube->m_Faces[4].m_Vertex1 = 6;
  cube->m_Faces[4].m_Vertex2 = 7;
  cube->m_Faces[4].m_Vertex3 = 3;

  cube->m_Faces[4].m_TVertex1 = 0;
  cube->m_Faces[4].m_TVertex2 = 1;
  cube->m_Faces[4].m_TVertex3 = 3;

  cube->m_Faces[5].m_Vertex1 = 3;
  cube->m_Faces[5].m_Vertex2 = 2;
  cube->m_Faces[5].m_Vertex3 = 6;

  cube->m_Faces[5].m_TVertex1 = 1;
  cube->m_Faces[5].m_TVertex2 = 2;
  cube->m_Faces[5].m_TVertex3 = 3;

  cube->m_Faces[6].m_Vertex1 = 5;
  cube->m_Faces[6].m_Vertex2 = 1;
  cube->m_Faces[6].m_Vertex3 = 0;

  cube->m_Faces[6].m_TVertex1 = 0;
  cube->m_Faces[6].m_TVertex2 = 1;
  cube->m_Faces[6].m_TVertex3 = 3;

  cube->m_Faces[7].m_Vertex1 = 4;
  cube->m_Faces[7].m_Vertex2 = 5;
  cube->m_Faces[7].m_Vertex3 = 0;

  cube->m_Faces[7].m_TVertex1 = 1;
  cube->m_Faces[7].m_TVertex2 = 2;
  cube->m_Faces[7].m_TVertex3 = 3;

  cube->m_Faces[8].m_Vertex1 = 7;
  cube->m_Faces[8].m_Vertex2 = 6;
  cube->m_Faces[8].m_Vertex3 = 5;

  cube->m_Faces[8].m_TVertex1 = 0;
  cube->m_Faces[8].m_TVertex2 = 1;
  cube->m_Faces[8].m_TVertex3 = 3;

  cube->m_Faces[9].m_Vertex1 = 7;
  cube->m_Faces[9].m_Vertex2 = 5;
  cube->m_Faces[9].m_Vertex3 = 4;

  cube->m_Faces[9].m_TVertex1 = 1;
  cube->m_Faces[9].m_TVertex2 = 2;
  cube->m_Faces[9].m_TVertex3 = 3;

  cube->m_Faces[10].m_Vertex1 = 4;
  cube->m_Faces[10].m_Vertex2 = 0;
  cube->m_Faces[10].m_Vertex3 = 3;

  cube->m_Faces[10].m_TVertex1 = 0;
  cube->m_Faces[10].m_TVertex2 = 1;
  cube->m_Faces[10].m_TVertex3 = 3;

  cube->m_Faces[11].m_Vertex1 = 4;
  cube->m_Faces[11].m_Vertex2 = 3;
  cube->m_Faces[11].m_Vertex3 = 7;

  cube->m_Faces[11].m_TVertex1 = 1;
  cube->m_Faces[11].m_TVertex2 = 2;
  cube->m_Faces[11].m_TVertex3 = 3;

  return cube;
} /* End of 'VEGraphicsGeometryCreateCube' function */

/***
 * PURPOSE: Delete geometry data
 *   PARAM: [IN] geometry - pointer to data to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGeometryDelete( VEGEOMETRY *geometry )
{
  if (!geometry)
    return;

  /* Release memory */
  Delete(geometry->m_Faces);
  Delete(geometry->m_Vertices);
  Delete(geometry->m_Positions);
  Delete(geometry->m_TCoordinates);
  Delete(geometry->m_Quaternions);
  Delete(geometry);
} /* End of 'VEGeometryDelete' function */

/***
 * PURPOSE: Find material by it's index
 *  RETURN: Pointer to material if success, FALSE otherwise
 *   PARAM: [IN] obj        - pointer to object (can be NULL)
 *   PARAM: [IN] materialID - index of material
 *  AUTHOR: Eliseev Dmitry
 ***/
VEMATERIAL *VEMaterialFind( const VEOBJECT *obj, const VEINT materialID )
{
  VEUINT matID = 0;

  /* Wrong object */
  if (!obj)
    return NULL;

  /* There are no materials */
  if (!obj->m_Materials)
    return NULL;

  /* Wrong material index */
  if (materialID == -1)
    return NULL;

  /* Search for material */
  for (matID = 0; matID < obj->m_NumMaterials; matID++)
    if (obj->m_Materials[matID]->m_MaterialID == materialID)
      return obj->m_Materials[matID];

  /* Material wasn't found */
  return NULL;
} /* End of 'VEMaterialFind' function */

VEBOUNDINGSPHERE VEMeshBoundingSphereCreateInternal( const VEMESH *mesh )
{
  VEUINT vertexID = 0;
  VEBOUNDINGSPHERE boundingSphere;
  memset(&boundingSphere, 0, sizeof(VEBOUNDINGSPHERE));

  /* Determine center */
  for (vertexID = 0; vertexID < mesh->m_NumVertices; vertexID++)
    boundingSphere.m_Position = VEVector3DAdd(boundingSphere.m_Position, mesh->m_Vertices[vertexID].m_Position);
  boundingSphere.m_Position = VEVector3DMult(boundingSphere.m_Position, 1.0 / (VEREAL)mesh->m_NumVertices);

  /* Determine radius */
  for (vertexID = 0; vertexID < mesh->m_NumVertices; vertexID++)
  {
    VEREAL currentDistance = VEVector3DDistanceSquare(mesh->m_Vertices[vertexID].m_Position, mesh->m_BoundingSphere.m_Position);
    if (currentDistance > boundingSphere.m_Radius)
      boundingSphere.m_Radius = currentDistance;
  }

  return boundingSphere;
} /* End of 'VEMeshBoundingSphereCreateInternal' function */

/***
 * PURPOSE: Transform object's geometry to renderable mesh
 *  RETURN: Pointer to created mesh if success, NULL otherwise
 *   PARAM: [IN] geom - pointer to created (readed) object's geometry
 *   PARAM: [IN] obj  - pointer to object (can be NULL)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEMESH *VEGeometryToMesh( const VEGEOMETRY *geom, const VEOBJECT *obj )
{
  VEUINT i = 0, vertexID = 0;
  VEMESH *mesh = New(sizeof(VEMESH), "New mesh");
  if (!mesh)
    return NULL;

  mesh->m_NumVertices = geom->m_NumFaces * 3;

  /* Memory allocation */
  mesh->m_Vertices = New(sizeof(VEVERTEX) * mesh->m_NumVertices, "Vertices");
  if (!mesh->m_Vertices)
  {
    Delete(mesh);
    return NULL;
  }

  /* Copying data */
  vertexID = 0;
  for (i = 0; i < geom->m_NumFaces; i++)
  {
    mesh->m_Vertices[vertexID+0].m_TCoordionates = geom->m_TCoordinates[geom->m_Faces[i].m_TVertex1];
    mesh->m_Vertices[vertexID+0].m_Position = geom->m_Vertices[geom->m_Faces[i].m_Vertex1].m_Position;
    mesh->m_Vertices[vertexID+0].m_Normal = geom->m_Vertices[geom->m_Faces[i].m_Vertex1].m_Normal;
    mesh->m_Vertices[vertexID+0].m_Color = VEVector4D(1.0, 1.0, 1.0, 1.0);

    mesh->m_Vertices[vertexID+1].m_TCoordionates = geom->m_TCoordinates[geom->m_Faces[i].m_TVertex2];
    mesh->m_Vertices[vertexID+1].m_Position = geom->m_Vertices[geom->m_Faces[i].m_Vertex2].m_Position;
    mesh->m_Vertices[vertexID+1].m_Normal = geom->m_Vertices[geom->m_Faces[i].m_Vertex2].m_Normal;
    mesh->m_Vertices[vertexID+1].m_Color = VEVector4D(1.0, 1.0, 1.0, 1.0);

    mesh->m_Vertices[vertexID+2].m_TCoordionates = geom->m_TCoordinates[geom->m_Faces[i].m_TVertex3];
    mesh->m_Vertices[vertexID+2].m_Position = geom->m_Vertices[geom->m_Faces[i].m_Vertex3].m_Position;
    mesh->m_Vertices[vertexID+2].m_Normal = geom->m_Vertices[geom->m_Faces[i].m_Vertex3].m_Normal;
    mesh->m_Vertices[vertexID+2].m_Color = VEVector4D(1.0, 1.0, 1.0, 1.0);

    vertexID += 3;
  }

  /* Build tangents */
  VEMeshBuildTangents(mesh);

  /* Store geometry */
  mesh->m_NumKeys = geom->m_NumKeys;
  if (mesh->m_NumKeys > 0)
  {
    /* Allocate mesh movement data array */
    mesh->m_Positions = New(mesh->m_NumKeys * sizeof(VEVECTOR3D), "Animation keys positions");
    if (!mesh->m_Positions)
    {
      Delete(mesh->m_Vertices);
      Delete(mesh);
      return NULL;
    }

    /* Allocate mesh movement data array */
    mesh->m_Quaternions = New(mesh->m_NumKeys * sizeof(VEVECTOR4D), "Animation keys rotations");
    if (!mesh->m_Quaternions)
    {
      Delete(mesh->m_Positions);
      Delete(mesh->m_Vertices);
      Delete(mesh);
      return NULL;
    }

    /* Allocate angles between neighbour quaternions array */
    mesh->m_QAngles = New(mesh->m_NumKeys * sizeof(VEREAL), "Angles between quaternions");
    if (!mesh->m_QAngles)
    {
      Delete(mesh->m_QAngles);
      Delete(mesh->m_Positions);
      Delete(mesh->m_Vertices);
      Delete(mesh);
      return NULL;
    }

    /* Copying data */
    memcpy(mesh->m_Positions,   geom->m_Positions,   sizeof(VEVECTOR3D) * mesh->m_NumKeys);
    memcpy(mesh->m_Quaternions, geom->m_Quaternions, sizeof(VEVECTOR4D) * mesh->m_NumKeys);

    /* Determine angles between quaternions */
    for (i = 0; i < mesh->m_NumKeys-1; i++)
      mesh->m_QAngles[i] = VEQuaternionDot(mesh->m_Quaternions[i], mesh->m_Quaternions[i+1]);
    mesh->m_QAngles[mesh->m_NumKeys-1] = VEQuaternionDot(mesh->m_Quaternions[mesh->m_NumKeys-1], mesh->m_Quaternions[0]);
  }

  /* Build bounding sphere */
  mesh->m_BoundingSphere = VEMeshBoundingSphereCreateInternal(mesh);

  /* Obtain buffer identifier for vertices array */
  glGenBuffersARB(1, &mesh->m_BufferID);

  /* Set current buffer */
  glBindBuffer(GL_ARRAY_BUFFER, mesh->m_BufferID);

  /* Prepare memory block to store data */
  glBufferData(GL_ARRAY_BUFFER, sizeof(VEVERTEX) * mesh->m_NumVertices, mesh->m_Vertices, GL_DYNAMIC_DRAW);

  /* Offset to vertex position */
  glEnableClientState(GL_VERTEX_ARRAY);
  glVertexPointer(3, GL_FLOAT, sizeof(VEVERTEX), BUFFER_OFFSET(0));

  /* Offset to normals */
  glEnableClientState(GL_NORMAL_ARRAY);
  glNormalPointer(GL_FLOAT, sizeof(VEVERTEX), BUFFER_OFFSET(12));

  /* Offset to texture coordinates */
  glEnableClientState(GL_TEXTURE_COORD_ARRAY);
  glTexCoordPointer(2, GL_FLOAT, sizeof(VEVERTEX), BUFFER_OFFSET(24));

  /* Offset to colors */
  glEnableClientState(GL_COLOR_ARRAY);
  glColorPointer(3, GL_FLOAT, sizeof(VEVERTEX), BUFFER_OFFSET(32));

  /* Bind material */
  mesh->m_Material = VEMaterialFind(obj, geom->m_MaterialID);

  /* That's it */
  return mesh;
} /* End of 'VEGeometryToMesh' function */
