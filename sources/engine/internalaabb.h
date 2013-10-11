#ifndef AABB_H_INCLUDED
#define AABB_H_INCLUDED

#include "vector3d.h"

/* Axes-aligned bounding box */
typedef struct tagVEAABB
{
  VEVECTOR3D m_Min; /* Minimal point of AABB */
  VEVECTOR3D m_Max; /* Maximal point of AABB */
} VEAABB;

/***
 * PURPOSE: Create new AABB
 *  RETURN: Created AABB
 *   PARAM: [IN] min - minimal AABB point
 *   PARAM: [IN] max - maximal AABB point
 *  AUTHOR: Eliseev Dmitry
 ***/
VEAABB VEAABBCreate( const VEVECTOR3D min, const VEVECTOR3D max );

/***
 * PURPOSE: Test if two AABB are intersects
 *  RETURN: TRUE if AABBs intersects, FALSE otherwise
 *   PARAM: [IN] box1 - first box
 *   PARAM: [IN] box2 - second box
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEAABBIsIntersects( const VEAABB box1, const VEAABB box2 );

#endif // AABB_H_INCLUDED
