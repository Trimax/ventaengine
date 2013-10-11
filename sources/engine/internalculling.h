#ifndef INTERNALCULLING_H_INCLUDED
#define INTERNALCULLING_H_INCLUDED

#include "internalaabb.h"
#include "vector3d.h"

/***
 * PURPOSE: Check if point at frustum
 *  RETURN: TRUE if point is visible, FALSE otherwise
 *   PARAM: [IN] pos - point coordinates
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsPointInFrustum( const VEVECTOR3D pos );

/***
 * PURPOSE: Check if sphere at frustum
 *  RETURN: TRUE if sphere is visible, FALSE otherwise
 *   PARAM: [IN] pos - point coordinates
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsSphereInFrustum( const VEVECTOR3D center, const VEREAL radius );

/***
 * PURPOSE: Check if cube at frustum
 *  RETURN: TRUE if cube is visible, FALSE otherwise
 *   PARAM: [IN] pos - point coordinates
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsCubeInFrustum( const VEVECTOR3D pos, VEREAL size );

/***
 * PURPOSE: Check AABB is in frustum
 *  RETURN: TRUE if AABB is visible, FALSE otherwise
 *   PARAM: [IN] pos - point coordinates
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsAABBInFrustum( const VEAABB box );

#endif // INTERNALCULLING_H_INCLUDED
