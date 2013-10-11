#ifndef CAMERA_H_INCLUDED
#define CAMERA_H_INCLUDED

#include "vector2d.h"
#include "vector3d.h"

/*** Common camera functions ***/

/***
 * PURPOSE: Create a new camera
 *  RETURN: Created camera identifier
 *   PARAM: [IN] sceneID - scene identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VECameraCreate( const VEUINT sceneID );

/***
 * PURPOSE: Select existing camera
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - scene identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VECameraSelect( const VEUINT sceneID, const VEUINT cameraID );

/***
 * PURPOSE: Attach camera to existing object
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] objectID - existing object identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraSetTarget( const VEUINT sceneID, const VEUINT cameraID, const VEUINT objectID );

/***
 * PURPOSE: Delete an existing camera
 *  RETURN: Created camera identifier
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraDelete( const VEUINT sceneID, const VEUINT cameraID );

/*** Free camera functions ***/

/***
 * PURPOSE: Set camera position
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] position - new camera position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraSetPosition( const VEUINT sceneID, const VEUINT cameraID, const VEVECTOR3D position );

/***
 * PURPOSE: Get camera position
 *  RETURN: Camera position
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VECameraGetPosition( const VEUINT sceneID, const VEUINT cameraID );

/***
 * PURPOSE: Set camera direction (for free cameras only)
 *   PARAM: [IN] sceneID   - scene identifier
 *   PARAM: [IN] cameraID  - existing camera identifier
 *   PARAM: [IN] direction - new camera direction
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraSetDirection( const VEUINT sceneID, const VEUINT cameraID, const VEVECTOR3D direction );

/***
 * PURPOSE: Move camera along the direction
 *   PARAM: [IN] sceneID   - scene identifier
 *   PARAM: [IN] cameraID  - existing camera identifier
 *   PARAM: [IN] value     - distance to move
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraMove( const VEUINT sceneID, const VEUINT cameraID, const VEREAL value );

/***
 * PURPOSE: Strafe camera (left-right)
 *   PARAM: [IN] sceneID   - scene identifier
 *   PARAM: [IN] cameraID  - existing camera identifier
 *   PARAM: [IN] value     - distance to move
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraStrafe( const VEUINT sceneID, const VEUINT cameraID, const VEREAL value );

/*** Attached camera functions ***/

/***
 * PURPOSE: Set camera distance to target object
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] distance - distance to target object
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraSetDistance( const VEUINT sceneID, const VEUINT cameraID, const VEREAL distance );

/***
 * PURPOSE: Get camera distance to target object
 *  RETURN: Curent camera distance to target object
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VECameraGetDistance( const VEUINT sceneID, const VEUINT cameraID );

/***
 * PURPOSE: Set camera angles
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] angles   - angles to set
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraSetAngles( const VEUINT sceneID, const VEUINT cameraID, const VEVECTOR2D angles );

/***
 * PURPOSE: Set camera yaw
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] yaw      - yaw angle
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraSetAngleYaw( const VEUINT sceneID, const VEUINT cameraID, const VEREAL yaw );

/***
 * PURPOSE: Set camera pitch
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] pitch    - pitch angle
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraSetAnglePitch( const VEUINT sceneID, const VEUINT cameraID, const VEREAL pitch );

/***
 * PURPOSE: Rotates camera by yaw
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] yaw      - angle to rotate
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraRotateYaw( const VEUINT sceneID, const VEUINT cameraID, const VEREAL yaw );

/***
 * PURPOSE: Rotates camera by pitch
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] pitch    - angle to rotate
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraRotatePitch( const VEUINT sceneID, const VEUINT cameraID, const VEREAL pitch );

#endif // CAMERA_H_INCLUDED
