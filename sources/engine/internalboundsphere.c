#include "internalresourcemanager.h"
#include "internalscenemanager.h"
#include "internalcontainer.h"
#include "internalobject.h"
#include "console.h"
#include "object.h"

#include <string.h>

/***
 * PURPOSE: Determine object's intersection using bounding spheres
 *  RETURN: TRUE if objects has intersection, FALSE otherwise
 *   PARAM: [IN] obj1 - pointer to object 1
 *   PARAM: [IN] obj1 - pointer to object 2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEObjectsIntersects( const VEOBJECT *obj1, const VEOBJECT *obj2 )
{
  VEREAL distance = VEVector3DDistance(VEVector3DAdd(obj1->m_BoundingSphere.m_Position, obj1->m_Position),
                                       VEVector3DAdd(obj2->m_BoundingSphere.m_Position, obj2->m_Position));
  VEREAL scale1   = VEMAX(VEMAX(obj1->m_Scaling.m_X, obj1->m_Scaling.m_Y), obj1->m_Scaling.m_Z);
  VEREAL scale2   = VEMAX(VEMAX(obj2->m_Scaling.m_X, obj2->m_Scaling.m_Y), obj2->m_Scaling.m_Z);

  return distance < (obj1->m_BoundingSphere.m_Radius * scale1 + obj2->m_BoundingSphere.m_Radius * scale2);
} /* End of 'VEObjectsIntersects' function */

/***
 * PURPOSE: Calculate the most far vertex distance
 *  RETURN: The most far vertex distance
 *   PARAM: [IN] position - point to calculate distance from
 *   PARAM: [IN] mesh     - pointer to mesh
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEMeshGetDistance( const VEVECTOR3D position, const VEMESH *mesh )
{
  VEREAL maxDistance = 0.0;
  VEUINT vertexID = 0;

  for (vertexID = 0; vertexID < mesh->m_NumVertices; vertexID++)
  {
    VEREAL distance = VEVector3DDistance(position, mesh->m_Vertices[vertexID].m_Position);
    if (distance > maxDistance)
      maxDistance = distance;
  }

  return maxDistance;
} /* End of 'VEMeshGetDistance' function */

/***
 * PURPOSE: Calculate object's bounding sphere
 *  RETURN: Object bounding sphere
 *   PARAM: [IN] object - pointer to object to build sphere
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOUNDINGSPHERE VEObjectBoundingSphereCreateInternal( const VEOBJECT *object )
{
  VEUINT meshID = 0;
  VEBOUNDINGSPHERE boundingSphere;
  memset(&boundingSphere, 0, sizeof(VEBOUNDINGSPHERE));

  /* Determine center */
  for (meshID = 0; meshID < object->m_NumMeshes; meshID++)
    boundingSphere.m_Position = VEVector3DAdd(boundingSphere.m_Position, object->m_Meshes[meshID]->m_BoundingSphere.m_Position);
  boundingSphere.m_Position = VEVector3DMult(boundingSphere.m_Position, 1.0 / (VEREAL)object->m_NumMeshes);

  /* Determine radius */
  for (meshID = 0; meshID < object->m_NumMeshes; meshID++)
  {
    VEREAL currentDistance = VEMeshGetDistance(boundingSphere.m_Position, object->m_Meshes[meshID]);
    if (currentDistance > boundingSphere.m_Radius)
      boundingSphere.m_Radius = currentDistance;
  }

  /* That's it */
  return boundingSphere;
} /* End of 'VEObjectBoundingSphereCreateInternal' function */
