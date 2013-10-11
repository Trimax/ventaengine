#include "internalsettings.h"
#include "internalculling.h"

/***
 * PURPOSE: Check if point at frustum
 *  RETURN: TRUE if point is visible, FALSE otherwise
 *   PARAM: [IN] pos - point coordinates
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsPointInFrustum( const VEVECTOR3D pos )
{
  VEINT planeID = 0;
  for (planeID = 0; planeID < 6; planeID++)
    if (p_SettingsGraphicsFrustum[planeID][0] * pos.m_X + p_SettingsGraphicsFrustum[planeID][1] * pos.m_Y + p_SettingsGraphicsFrustum[planeID][2] * pos.m_Z + p_SettingsGraphicsFrustum[planeID][3] <= 0)
      return FALSE;
  return TRUE;
} /* End of 'VEIsPointInFrustum' function */

/***
 * PURPOSE: Check if sphere at frustum
 *  RETURN: TRUE if sphere is visible, FALSE otherwise
 *   PARAM: [IN] pos - point coordinates
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsSphereInFrustum( const VEVECTOR3D pos, const VEREAL radius )
{
  VEINT planeID = 0;
  for (planeID = 0; planeID < 6; planeID++)
    if (p_SettingsGraphicsFrustum[planeID][0] * pos.m_X + p_SettingsGraphicsFrustum[planeID][1] * pos.m_Y + p_SettingsGraphicsFrustum[planeID][2] * pos.m_Z + p_SettingsGraphicsFrustum[planeID][3] <= -radius)
      return FALSE;
  return TRUE;
} /* End of 'VEIsSphereInFrustum' function */

/***
 * PURPOSE: Check if cube at frustum
 *  RETURN: TRUE if cube is visible, FALSE otherwise
 *   PARAM: [IN] pos - point coordinates
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsCubeInFrustum( const VEVECTOR3D pos, VEREAL size )
{
  VEINT planeID = 0;
  for(planeID = 0; planeID < 6; planeID++)
  {
    if (p_SettingsGraphicsFrustum[planeID][0] * (pos.m_X - size) + p_SettingsGraphicsFrustum[planeID][1] * (pos.m_Y - size) + p_SettingsGraphicsFrustum[planeID][2] * (pos.m_Z - size) + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    if (p_SettingsGraphicsFrustum[planeID][0] * (pos.m_X + size) + p_SettingsGraphicsFrustum[planeID][1] * (pos.m_Y - size) + p_SettingsGraphicsFrustum[planeID][2] * (pos.m_Z - size) + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    if (p_SettingsGraphicsFrustum[planeID][0] * (pos.m_X - size) + p_SettingsGraphicsFrustum[planeID][1] * (pos.m_Y + size) + p_SettingsGraphicsFrustum[planeID][2] * (pos.m_Z - size) + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    if (p_SettingsGraphicsFrustum[planeID][0] * (pos.m_X + size) + p_SettingsGraphicsFrustum[planeID][1] * (pos.m_Y + size) + p_SettingsGraphicsFrustum[planeID][2] * (pos.m_Z - size) + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    if (p_SettingsGraphicsFrustum[planeID][0] * (pos.m_X - size) + p_SettingsGraphicsFrustum[planeID][1] * (pos.m_Y - size) + p_SettingsGraphicsFrustum[planeID][2] * (pos.m_Z + size) + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    if (p_SettingsGraphicsFrustum[planeID][0] * (pos.m_X + size) + p_SettingsGraphicsFrustum[planeID][1] * (pos.m_Y - size) + p_SettingsGraphicsFrustum[planeID][2] * (pos.m_Z + size) + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    if (p_SettingsGraphicsFrustum[planeID][0] * (pos.m_X - size) + p_SettingsGraphicsFrustum[planeID][1] * (pos.m_Y + size) + p_SettingsGraphicsFrustum[planeID][2] * (pos.m_Z + size) + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    if (p_SettingsGraphicsFrustum[planeID][0] * (pos.m_X + size) + p_SettingsGraphicsFrustum[planeID][1] * (pos.m_Y + size) + p_SettingsGraphicsFrustum[planeID][2] * (pos.m_Z + size) + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    return FALSE;
  }

  /* That's it */
  return TRUE;
} /* End of 'VEIsCubeInFrustum' function */

/***
 * PURPOSE: Check AABB is in frustum
 *  RETURN: TRUE if AABB is visible, FALSE otherwise
 *   PARAM: [IN] pos - point coordinates
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsAABBInFrustum( const VEAABB box )
{
  VEINT planeID = 0;
  for(planeID = 0; planeID < 6; planeID++)
  {
    if (p_SettingsGraphicsFrustum[planeID][0] * box.m_Min.m_X + p_SettingsGraphicsFrustum[planeID][1] * box.m_Min.m_Y + p_SettingsGraphicsFrustum[planeID][2] * box.m_Min.m_Z + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    if (p_SettingsGraphicsFrustum[planeID][0] * box.m_Max.m_X + p_SettingsGraphicsFrustum[planeID][1] * box.m_Min.m_Y + p_SettingsGraphicsFrustum[planeID][2] * box.m_Min.m_Z + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    if (p_SettingsGraphicsFrustum[planeID][0] * box.m_Min.m_X + p_SettingsGraphicsFrustum[planeID][1] * box.m_Max.m_Y + p_SettingsGraphicsFrustum[planeID][2] * box.m_Min.m_Z + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    if (p_SettingsGraphicsFrustum[planeID][0] * box.m_Max.m_X + p_SettingsGraphicsFrustum[planeID][1] * box.m_Max.m_Y + p_SettingsGraphicsFrustum[planeID][2] * box.m_Min.m_Z + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    if (p_SettingsGraphicsFrustum[planeID][0] * box.m_Min.m_X + p_SettingsGraphicsFrustum[planeID][1] * box.m_Min.m_Y + p_SettingsGraphicsFrustum[planeID][2] * box.m_Max.m_Z + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    if (p_SettingsGraphicsFrustum[planeID][0] * box.m_Max.m_X + p_SettingsGraphicsFrustum[planeID][1] * box.m_Min.m_Y + p_SettingsGraphicsFrustum[planeID][2] * box.m_Max.m_Z + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    if (p_SettingsGraphicsFrustum[planeID][0] * box.m_Min.m_X + p_SettingsGraphicsFrustum[planeID][1] * box.m_Max.m_Y + p_SettingsGraphicsFrustum[planeID][2] * box.m_Max.m_Z + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    if (p_SettingsGraphicsFrustum[planeID][0] * box.m_Max.m_X + p_SettingsGraphicsFrustum[planeID][1] * box.m_Max.m_Y + p_SettingsGraphicsFrustum[planeID][2] * box.m_Max.m_Z + p_SettingsGraphicsFrustum[planeID][3] > 0)
      continue;
    return FALSE;
  }

  /* That's it */
  return TRUE;
} /* End of 'VEIsAABBInFrustum' function */
