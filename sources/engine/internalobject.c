#include "internalglut.h"

#include "internalscenemanager.h"
#include "internalgeometry.h"
#include "internalmaterial.h"
#include "internalsettings.h"
#include "internalobject.h"
#include "internalheader.h"
#include "memorymanager.h"
#include "internalmesh.h"
#include "console.h"

#include <string.h>

/***
 * PURPOSE: Load object from a file
 *  RETURN: Pointer to object if success, NULL otherwise
 *   PARAM: [IN] f - pointer to file stream
 *  AUTHOR: Eliseev Dmitry
 ***/
VEOBJECT *VEObjectLoadInternal( FILE *f )
{
  VEUINT materialID = 0, meshID = 0;

  VEHEADER header;
  VEOBJECT *object = New(sizeof(VEOBJECT), "Scene object");
  if (!object)
    return NULL;

  /* Reading file header */
  fread(&header, 1, sizeof(VEHEADER), f);
  if (!VEHeaderIsValid(header, VE_FORMAT_OBJECT))
  {
    VEObjectDeleteInternal(object);
    return NULL;
  }

  /* Reading materials number */
  fread(&object->m_NumMaterials, 1, sizeof(VEUINT), f);

  /* Reading materials */
  if (object->m_NumMaterials > 0)
  {
    /* Allocate memory for materials */
    object->m_Materials = New(sizeof(VEMATERIAL*) * object->m_NumMaterials, "Object's materials");
    if (!object->m_Materials)
    {
      VEObjectDeleteInternal(object);
      return NULL;
    }

    /* Reading materials */
    for (materialID = 0; materialID < object->m_NumMaterials; materialID++)
    {
      object->m_Materials[materialID] = VEMaterialLoad(f);
      if (!object->m_Materials[materialID])
      {
        VEObjectDeleteInternal(object);
        return NULL;
      }
    }
  }

  /* Reading the number of meshes */
  fread(&object->m_NumMeshes, 1, sizeof(VEUINT), f);

  /* Reading number of keys and frame rate */
  fread(&object->m_NumFrames, 1, sizeof(VEUINT), f);
  fread(&object->m_FrameRate, 1, sizeof(VEUINT), f);

  /* Reading meshes */
  if (object->m_NumMeshes > 0)
  {
    /* Meshes array creation */
    object->m_Meshes = New(sizeof(VEMESH*) * object->m_NumMeshes, "Object meshes");
    if (!object->m_Meshes)
    {
      VEObjectDeleteInternal(object);
      return NULL;
    }

    /* Reading meshes */
    for (meshID = 0; meshID < object->m_NumMeshes; meshID++)
    {
      VEGEOMETRY *geometry = VEGeometryLoad(f, object->m_NumFrames);
      if (!geometry)
      {
        VEObjectDeleteInternal(object);
        return NULL;
      }

      /* Build per-vertex normals */
      VEGeometryBuildNormals(geometry);

      /* Convert geometry to mesh */
      object->m_Meshes[meshID] = VEGeometryToMesh(geometry, object);
      VEGeometryDelete(geometry);
      if (!object->m_Meshes[meshID])
      {
        VEObjectDeleteInternal(object);
        return NULL;
      }
    }
  }

  /* Default scaling */
  object->m_Scaling = VEVector3D(1.0, 1.0, 1.0);

  /* That's it */
  return object;
} /* End of 'VEObjectLoad' function */

/***
 * PURPOSE: Render object
 *   PARAM: [IN] object      - pointer to object to render
 *   PARAM: [IN] timeElapsed - pointer to elapsed time since last update
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectUpdate( VEOBJECT *object, VEREAL *timeElapsed )
{
  VEREAL t = *timeElapsed;
  object->m_Animation.m_LocalTime += t * object->m_Animation.m_TimeFactor;
  if (object->m_Animation.m_LocalTime > 1000.0)
    object->m_Animation.m_LocalTime -= 1000.0;

  /* If there is an animation */
  if (object->m_NumFrames > 0)
  {
    VEREAL frameRate = 1000.0 / object->m_NumFrames;
    object->m_Animation.m_FrameID = (VEUINT)(object->m_Animation.m_LocalTime / frameRate);
    object->m_Animation.m_FramePercent = (1.0 - 0.01 * (frameRate * (object->m_Animation.m_FrameID + 1.0) - object->m_Animation.m_LocalTime));

    printf("ID = %d; dt = %.2lf; rate = %.2lf; time = %.2lf\n", object->m_Animation.m_FrameID, object->m_Animation.m_FramePercent, frameRate, object->m_Animation.m_LocalTime);
  }
} /* End of 'VEObjectUpdate' function */

/***
 * PURPOSE: Render object
 *   PARAM: [IN] object - pointer to object to render
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectRender( VEOBJECT *object )
{
  VEUINT meshID = 0;

  /* Store matrix */
  glPushMatrix();
  glLoadIdentity();

  /* Translate object */
  glTranslatef(object->m_Position.m_X, object->m_Position.m_Y, object->m_Position.m_Z);

  /* Rotate object */
  glRotatef(object->m_Rotation.m_X, 1.0, 0.0, 0.0);
  glRotatef(object->m_Rotation.m_Y, 0.0, 1.0, 0.0);
  glRotatef(object->m_Rotation.m_Z, 0.0, 0.0, 1.0);

  /* Scale object */
  glScalef(object->m_Scaling.m_X, object->m_Scaling.m_Y, object->m_Scaling.m_Z);

  /* Render meshes */
  for (meshID = 0; meshID < object->m_NumMeshes; meshID++)
    VEMeshRender(object->m_Meshes[meshID]);

  /* Restore matrix */
  glPopMatrix();

  /* Render forces */
  if (p_SettingsGraphicsRenderForces)
  {
    VEVECTOR3D normalizedForce = VEVector3DNormalize(object->m_Force);
    glDisable(GL_LIGHTING);
    glBegin(GL_LINES);
      /* X (red) */
      glColor3d(1.0, 1.0, 0.0);

      glVertex3d(object->m_Position.m_X, object->m_Position.m_Y, object->m_Position.m_Z);
      glVertex3d(object->m_Position.m_X + normalizedForce.m_X, object->m_Position.m_Y + normalizedForce.m_Y, object->m_Position.m_Z + normalizedForce.m_Z);
    glEnd();
    glEnable(GL_LIGHTING);
  }
} /* End of 'VEObjectRender' function */

/***
 * PURPOSE: Delete object
 *   PARAM: [IN] object - pointer to object to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectDeleteInternal( VEOBJECT *object )
{
  VEUINT id = 0;
  if (!object)
    return;

  /* Delete meshes */
  if (object->m_Meshes)
    for (id = 0; id < object->m_NumMeshes; id++)
      VEMeshDelete(object->m_Meshes[id]);
  Delete(object->m_Meshes);

  /* Delete materials */
  if (object->m_Materials)
    for (id = 0; id < object->m_NumMaterials; id++)
      Delete(object->m_Materials[id]);
  Delete(object->m_Materials);

  /* Delete object */
  Delete(object);
} /* End of 'VEObjectDeleteInternal' function */

/***
 * PURPOSE: Print object information to console
 *   PARAM: [IN] object - object to print information
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectPrint( VEOBJECT *object )
{
  VECHAR objectInfo[VE_BUFFER_STANDART];
  memset(objectInfo, 0, VE_BUFFER_STANDART);

  /* Print scene information to console */
  sprintf(objectInfo, "ID: %d; Pos: [%.2lf; %.2lf; %.2lf]; Vel: [%.2lf; %.2lf; %.2lf]; Mass: %.2lf",
          object->m_ID,
          object->m_Position.m_X,    object->m_Position.m_Y,    object->m_Position.m_Z,
          object->m_VelPosition.m_X, object->m_VelPosition.m_Y, object->m_VelPosition.m_Z,
          object->m_Mass);

  VEConsolePrint(objectInfo);
} /* End of 'VEObjectPrint' function */

/***
 * PURPOSE: Print objects information to console
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectList( VEVOID )
{
  VESCENE *scene = VESceneGet(p_SettingsGraphicsSceneID);
  VEInternalContainerForeach((VEINTERNALCONTAINER*)scene->m_Objects, (VEFUNCTION)VEObjectPrint);
} /* End of 'VEObjectList' function */
