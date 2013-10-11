#include "internalglut.h"

#include "internalshadermanager.h"
#include "internalgeometry.h"
#include "internalsettings.h"
#include "memorymanager.h"
#include "internalmesh.h"
#include "vector4d.h"

#include <string.h>

/***
 * PURPOSE: Create a simple cube mesh
 *  RETURN: Pointer to created mesh if success, NULL otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEMESH *VEMeshCreateCube( VEVOID )
{
  VEMESH *cubeMesh = NULL;
  VEGEOMETRY *cubeGeom = VEGeometryCreateCube();
  if (!cubeGeom)
    return NULL;

  /* Transform geometry to a mesh */
  cubeMesh = VEGeometryToMesh(cubeGeom, NULL);
  VEGeometryDelete(cubeGeom);

  /* That's it */
  return cubeMesh;
} /* End of 'VEGraphicsMeshCreateCube' function */

/***
 * PURPOSE: Delete mesh data
 *   PARAM: [IN] mesh - pointer to mesh to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMeshDelete( VEMESH *mesh )
{
  if (!mesh)
    return;

  if (mesh->m_BufferID)
    glDeleteBuffers(1, &mesh->m_BufferID);

  /* Release memory */
  Delete(mesh->m_Vertices);
  Delete(mesh);
} /* End of 'VEGraphicsMeshDelete' function */

/***
 * PURPOSE: Render mesh data slowly (per face)
 *   PARAM: [IN]  mesh - pointer to mesh to render
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMeshRenderSlow( VEMESH *mesh )
{
  VEUINT numFaces = mesh->m_NumVertices / 3;
  VEUINT faceID = 0, indexID = 0;

  /* Render faces */
  glBegin(p_SettingsGraphicsRenderType);

  /* Faces rendering */
  for (faceID = 0; faceID < numFaces; faceID++)
    for (indexID = 0; indexID < 3; indexID++)
    {
      VEVERTEX v = mesh->m_Vertices[3 * faceID + indexID];

      /* Set vertex */
      glNormal3f(v.m_Normal.m_X, v.m_Normal.m_Y, v.m_Normal.m_Z);
      glColor3f(v.m_Color.m_X, v.m_Color.m_Y, v.m_Color.m_Z);
      glTexCoord2f(v.m_TCoordionates.m_X, v.m_TCoordionates.m_Y);
      glVertex3f(v.m_Position.m_X, v.m_Position.m_Y, v.m_Position.m_Z);
    }

  /* That's it */
  glEnd();
} /* End of 'VEMeshRenderSlow' function */

/***
 * PURPOSE: Render per vertex tangent space
 *   PARAM: [IN]  mesh - pointer to mesh to render
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMeshRenderTBN( VEMESH *mesh )
{
  VEUINT vertexID = 0;

  glDisable(GL_TEXTURE_2D);
  glDisable(GL_LIGHTING);

  /* Render faces */
  glBegin(GL_LINES);

  /* Faces rendering */
  for (vertexID = 0; vertexID < mesh->m_NumVertices; vertexID++)
  {
    VEVERTEX v = mesh->m_Vertices[vertexID];

    /* Calculate TBN end points */
    VEVECTOR3D tDir = VEVector3DAdd(v.m_Position, VEVector3DMult(VEVector3DNormalize(VEVector4DXYZ(v.m_Tangent)), 0.1));
    VEVECTOR3D bDir = VEVector3DAdd(v.m_Position, VEVector3DMult(VEVector3DNormalize(v.m_Binormal), 0.1));
    VEVECTOR3D nDir = VEVector3DAdd(v.m_Position, VEVector3DMult(VEVector3DNormalize(v.m_Normal), 0.1));

    /* Tangent */
    glColor3d(1.0, 0.0, 0.0);
    glVertex3d(v.m_Position.m_X, v.m_Position.m_Y, v.m_Position.m_Z);
    glVertex3d(tDir.m_X, tDir.m_Y, tDir.m_Z);

    /* Binormal */
    glColor3d(0.0, 1.0, 0.0);
    glVertex3d(v.m_Position.m_X, v.m_Position.m_Y, v.m_Position.m_Z);
    glVertex3d(bDir.m_X, bDir.m_Y, bDir.m_Z);

    /* Normal */
    glColor3d(0.0, 0.0, 1.0);
    glVertex3d(v.m_Position.m_X, v.m_Position.m_Y, v.m_Position.m_Z);
    glVertex3d(nDir.m_X, nDir.m_Y, nDir.m_Z);
  }

  /* That's it */
  glEnd();

  glEnable(GL_TEXTURE_2D);
  glEnable(GL_LIGHTING);
} /* End of 'VEMeshRenderTBN' function */

/***
 * PURPOSE: Render mesh data
 *   PARAM: [IN]  mesh - pointer to mesh to render
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMeshRender( VEMESH *mesh )
{
  if (!mesh)
    return;

  /* Disable any shader before rendering */
  glUseProgram(0);

  if (p_SettingsGraphicsRenderSlow)
    VEMeshRenderTBN(mesh);

  /* Apply program */
  if ((mesh->m_ProgramID != 0)&&(p_SettingsGraphicsShaders))
    VEProgramUseInternal(mesh);
  else if (mesh->m_Material)
  {
    glEnable(GL_TEXTURE_2D);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, mesh->m_Material->m_MapDiffuse);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE_EXT);
    glTexEnvf(GL_TEXTURE_ENV, GL_COMBINE_RGB_EXT, GL_REPLACE);
  }

  /* Which one: fast or slow? */
  if ((p_SettingsGraphicsRenderSlow)||(!p_SettingsGraphicsVBOSupported))
    VEMeshRenderSlow(mesh);
  else
  {
    VEINT tangents  = glGetAttribLocation(mesh->m_ProgramID, VE_SHADERPARAM_TANGENTS);
    VEINT binormals = glGetAttribLocation(mesh->m_ProgramID, VE_SHADERPARAM_BINORMALS);

    /* Enable arrays */
    glEnableClientState(GL_NORMAL_ARRAY);
    glEnableClientState(GL_COLOR_ARRAY);
    glEnableClientState(GL_VERTEX_ARRAY);
    glEnableClientState(GL_TEXTURE_COORD_ARRAY);

    /* Select vertex buffer */
    glBindBuffer(GL_ARRAY_BUFFER, mesh->m_BufferID);

    /* Bind pointers */
    glVertexPointer(3, GL_FLOAT,   sizeof(VEVERTEX), BUFFER_OFFSET(0));
    glNormalPointer(GL_FLOAT,      sizeof(VEVERTEX), BUFFER_OFFSET(12));
    glTexCoordPointer(2, GL_FLOAT, sizeof(VEVERTEX), BUFFER_OFFSET(24));
    glColorPointer(3, GL_FLOAT,    sizeof(VEVERTEX), BUFFER_OFFSET(32));
    if (tangents > 0)
      glVertexAttribPointer(tangents, 4, GL_FLOAT, GL_TRUE, sizeof(VEVERTEX), BUFFER_OFFSET(48));
    if (binormals > 0)
      glVertexAttribPointer(binormals, 4, GL_FLOAT, GL_TRUE, sizeof(VEVERTEX), BUFFER_OFFSET(64));

    /* Reset mesh color to white */
    glColor4f(1.0, 1.0, 1.0, 0.0);

    /* Draw arrays using vertices buffer */
    glDrawArrays(p_SettingsGraphicsRenderType, 0, mesh->m_NumVertices);

    /* Unbind buffers */
    glBindBuffer(GL_ARRAY_BUFFER,        0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    /* Disable arrays */
    glDisableClientState(GL_TEXTURE_COORD_ARRAY);
    glDisableClientState(GL_VERTEX_ARRAY);
    glDisableClientState(GL_COLOR_ARRAY);
    glDisableClientState(GL_NORMAL_ARRAY);
  }

  /* Unbind texture */
  glBindTexture(GL_TEXTURE_2D, 0);
  glDisable(GL_TEXTURE_2D);

  /* Disable program using */
  if ((mesh->m_ProgramID)&&(p_SettingsGraphicsShaders))
    glUseProgram(0);
} /* End of 'VEMeshRender' function */

/***
 * PURPOSE: Build per-vertex tangents
 *   PARAM: [IN]  mesh - pointer to mesh to render
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMeshBuildTangents( VEMESH *mesh )
{
  VEVECTOR3D *tempTangents = NULL;
  VEUINT vertexID = 0, faceID = 0, numFaces = 0;

  /* Wrong arguments */
  if (!mesh)
    return;

  /* Allocate temporary tangents array */
  tempTangents = New(sizeof(VEVECTOR4D) * mesh->m_NumVertices * 2, "Temporary tangents");
  if (!tempTangents)
    return;

  /* Determine the number of faces */
  numFaces = mesh->m_NumVertices / 3;
  for (faceID = 0; faceID < numFaces; faceID++)
  {
    VEUINT i1 = 3 * faceID + 0;
    VEUINT i2 = 3 * faceID + 1;
    VEUINT i3 = 3 * faceID + 2;

    VEVERTEX v1 = mesh->m_Vertices[i1];
    VEVERTEX v2 = mesh->m_Vertices[i2];
    VEVERTEX v3 = mesh->m_Vertices[i3];

    VEREAL x1 = v2.m_Position.m_X - v1.m_Position.m_X;
    VEREAL x2 = v3.m_Position.m_X - v1.m_Position.m_X;
    VEREAL y1 = v2.m_Position.m_Y - v1.m_Position.m_Y;
    VEREAL y2 = v3.m_Position.m_Y - v1.m_Position.m_Y;
    VEREAL z1 = v2.m_Position.m_Z - v1.m_Position.m_Z;
    VEREAL z2 = v3.m_Position.m_Z - v1.m_Position.m_Z;

    VEREAL s1 = v2.m_TCoordionates.m_X - v1.m_TCoordionates.m_X;
    VEREAL s2 = v3.m_TCoordionates.m_X - v1.m_TCoordionates.m_X;
    VEREAL t1 = v2.m_TCoordionates.m_Y - v1.m_TCoordionates.m_Y;
    VEREAL t2 = v3.m_TCoordionates.m_Y - v1.m_TCoordionates.m_Y;

    VEREAL r = 1.0F / (s1 * t2 - s2 * t1);

    VEVECTOR3D sDir = VEVector3D((t2 * x1 - t1 * x2) * r, (t2 * y1 - t1 * y2) * r, (t2 * z1 - t1 * z2) * r);
    VEVECTOR3D tDir = VEVector3D((s1 * x2 - s2 * x1) * r, (s1 * y2 - s2 * y1) * r, (s1 * z2 - s2 * z1) * r);

    tempTangents[i1] = VEVector3DAdd(tempTangents[i1], sDir);
    tempTangents[i2] = VEVector3DAdd(tempTangents[i2], sDir);
    tempTangents[i3] = VEVector3DAdd(tempTangents[i3], sDir);

    tempTangents[i1 + mesh->m_NumVertices] = VEVector3DAdd(tempTangents[i1 + mesh->m_NumVertices], tDir);
    tempTangents[i2 + mesh->m_NumVertices] = VEVector3DAdd(tempTangents[i2 + mesh->m_NumVertices], tDir);
    tempTangents[i3 + mesh->m_NumVertices] = VEVector3DAdd(tempTangents[i3 + mesh->m_NumVertices], tDir);
  }

  /* Store tangents for each vertex */
  for (vertexID = 0; vertexID < mesh->m_NumVertices; vertexID++)
  {
    VEVECTOR3D normal  = mesh->m_Vertices[vertexID].m_Normal;
    VEVECTOR3D tangent = tempTangents[vertexID];

    /* Gram-Schmidt orthogonalize */
    VEVECTOR3D temp = VEVector3DNormalize(VEVector3DSub(tangent, VEVector3DMult(normal, VEVector3DDot(normal, tangent))));
    memcpy(&mesh->m_Vertices[vertexID].m_Tangent, &temp, sizeof(VEVECTOR3D));
    mesh->m_Vertices[vertexID].m_Tangent.m_W = (VEVector3DDot(VEVector3DCross(normal, tangent), tempTangents[vertexID + mesh->m_NumVertices]) < 0.0F) ? -1.0F : 1.0F;

    /* Calculate binormal */
    mesh->m_Vertices[vertexID].m_Binormal = VEVector3DNormalize(VEVector3DCross(normal, VEVector4DXYZ(mesh->m_Vertices[vertexID].m_Tangent)));
  }

  /* Release memory */
  Delete(tempTangents);
} /* End of 'VEMeshBuildTangents' function */
