#include "internalaabb.h"

/***
 * PURPOSE: Create new AABB
 *  RETURN: Created AABB
 *   PARAM: [IN] min - minimal AABB point
 *   PARAM: [IN] max - maximal AABB point
 *  AUTHOR: Eliseev Dmitry
 ***/
VEAABB VEAABBCreate( const VEVECTOR3D min, const VEVECTOR3D max )
{
  VEAABB box = {min, max};
  return box;
} /* End of 'VEAABBCreate' function */

/***
 * PURPOSE: Test if two AABB are intersects
 *  RETURN: TRUE if AABBs intersects, FALSE otherwise
 *   PARAM: [IN] box1 - first box
 *   PARAM: [IN] box2 - second box
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEAABBIsIntersects( const VEAABB box1, const VEAABB box2 )
{
  if (box1.m_Max.m_X < box2.m_Min.m_X || box1.m_Min.m_X > box2.m_Max.m_X)
    return FALSE;
  if (box1.m_Max.m_Y < box2.m_Min.m_Y || box1.m_Min.m_Y > box2.m_Max.m_Y)
    return FALSE;
  if (box1.m_Max.m_Z < box2.m_Min.m_Z || box1.m_Min.m_Z > box2.m_Max.m_Z)
    return FALSE;
  return TRUE;
} /* End of 'VEAABBIsIntersects' function */
