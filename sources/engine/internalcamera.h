#ifndef INTERNALCAMERA_H_INCLUDED
#define INTERNALCAMERA_H_INCLUDED

#include "vector2d.h"
#include "vector3d.h"

/* Camera structure */
typedef struct tagVECAMERA
{
  VEUINT     m_ID;         /* Self identifier */
  VEVECTOR3D m_Position;   /* Position */
  VEVECTOR3D m_LookAt;     /* Look at point */

  /*** Attached camera parameters ***/
  VEREAL     m_Distance; /* Distance to object */
  VEVECTOR2D m_Angles;   /* Spherical coordinates */
  VEUINT     m_TargetID; /* Target identifier */
} VECAMERA;

/***
 * PURPOSE: Update all attached cameras position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEUpdateCameras( VEVOID );

/***
 * PURPOSE: Print camera list to console
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraList( VEVOID );

/***
 * PURPOSE: Render all lights of scene as spheres
 *   PARAM: [IN] sceneID - scene identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraRender( const VEUINT sceneID );

#endif // INTERNALCAMERA_H_INCLUDED
