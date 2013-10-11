#include "internalglut.h"

#include "internalscenemanager.h"
#include "internalsettings.h"
#include "internalcamera.h"
#include "internalobject.h"
#include "memorymanager.h"
#include "console.h"
#include "camera.h"

#include <string.h>
#include <math.h>

#define VE_DEGTORAD(x) (((x)*M_PI) / 180.0)

/***
 * PURPOSE: Update camera
 *   PARAM: [IN] camera - pointer to camera
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraUpdate( VECAMERA *camera )
{
  camera->m_LookAt.m_X = camera->m_Position.m_X + camera->m_Distance * cos(camera->m_Angles.m_Y) * cos(camera->m_Angles.m_X);
  camera->m_LookAt.m_Y = camera->m_Position.m_Y + camera->m_Distance * sin(camera->m_Angles.m_Y);
  camera->m_LookAt.m_Z = camera->m_Position.m_Z + camera->m_Distance * cos(camera->m_Angles.m_Y) * sin(camera->m_Angles.m_X);
} /* End of '' function */

/***
 * PURPOSE: Create a new camera
 *  RETURN: Created camera identifier
 *   PARAM: [IN] sceneID - scene identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VECameraCreate( const VEUINT sceneID )
{
  VECAMERA *camera = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  /* Get scene */
  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return 0;
  }

  /* Camera creation */
  camera = New(sizeof(VECAMERA), "Camera");
  if (!camera)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return 0;
  }

  /* Default camera direction is along applicate */
  camera->m_LookAt   = VEVector3D(0.0, 0.0, 1.0);
  camera->m_Distance = 1.0;

  camera->m_ID = VEInternalContainerAdd((VEINTERNALCONTAINER*)scene->m_Cameras, camera);
  VESectionLeaveInternal(p_SettingsGraphicsSection);

  /* Add camera to scene*/
  return camera->m_ID;
} /* End of 'VECameraCreate' function */

/***
 * PURPOSE: Set camera position
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] position - new camera position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraSetPosition( const VEUINT sceneID, const VEUINT cameraID, const VEVECTOR3D position )
{
  VECAMERA *camera = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  /* Get scene */
  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Set camera position */
  camera = VEInternalContainerGet(scene->m_Cameras, cameraID);
  if (camera)
  {
    VEVECTOR3D direction = VEVector3DSub(camera->m_LookAt, camera->m_Position);
    camera->m_Position = position;
    camera->m_LookAt = VEVector3DAdd(camera->m_Position, direction);
  }

  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VECameraSetPosition' function */

/***
 * PURPOSE: Get camera position
 *  RETURN: Camera position
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VECameraGetPosition( const VEUINT sceneID, const VEUINT cameraID )
{
  VEVECTOR3D position = VEVector3D(0.0, 0.0, 0.0);
  VECAMERA *camera = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  /* Get scene */
  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return position;
  }

  /* Get camera position */
  camera = VEInternalContainerGet(scene->m_Cameras, cameraID);
  if (camera)
    position = camera->m_Position;

  VESectionLeaveInternal(p_SettingsGraphicsSection);

  /* That's it */
  return position;
} /* End of 'VECameraGetPosition' function */

/***
 * PURPOSE: Set camera direction (for free cameras only)
 *   PARAM: [IN] sceneID   - scene identifier
 *   PARAM: [IN] cameraId  - existing camera identifier
 *   PARAM: [IN] direction - new camera direction
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraSetDirection( const VEUINT sceneID, const VEUINT cameraID, const VEVECTOR3D direction )
{
  VECAMERA *camera = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  /* Get scene */
  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Set camera position */
  camera = VEInternalContainerGet(scene->m_Cameras, cameraID);
  if (camera)
    camera->m_LookAt = VEVector3DAdd(camera->m_Position, direction);

  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VECameraSetDirection' function */

/***
 * PURPOSE: Delete an existing camera
 *  RETURN: Created camera identifier
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraId - existing camera identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraDelete( const VEUINT sceneID, const VEUINT cameraID )
{
  VECAMERA *camera = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Reset camera to default */
  if (scene->m_CameraID == cameraID)
    scene->m_CameraID = 0;

  /* Remove camera from internal container */
  camera = VEInternalContainerGet(scene->m_Cameras, cameraID);
  VEInternalContainerRemove(scene->m_Cameras, cameraID);

  /* Release memory */
  Delete(camera);

  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VECameraDelete' function */

/***
 * PURPOSE: Select existing camera
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - scene identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VECameraSelect( const VEUINT sceneID, const VEUINT cameraID )
{
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return FALSE;
  }

  /* Select camera */
  if (!VEInternalContainerGet(scene->m_Cameras, cameraID))
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return FALSE;
  }

  /* Select camera */
  scene->m_CameraID = cameraID;

  VESectionLeaveInternal(p_SettingsGraphicsSection);
  return TRUE;
} /* End of 'VECameraSelect' function */

/***
 * PURPOSE: Attach camera to existing object
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] objectID - existing object identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraSetTarget( const VEUINT sceneID, const VEUINT cameraID, const VEUINT objectID )
{
  VEOBJECT *object = NULL;
  VECAMERA *camera = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Get camera */
  camera = VEInternalContainerGet(scene->m_Cameras, cameraID);
  if (!camera)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Detach camera */
  if (objectID == 0)
  {
    camera->m_TargetID = 0;
    camera->m_Distance = 1.0;
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Get object */
  object = VEInternalContainerGet(scene->m_Objects, objectID);
  if (!object)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Attach camera to object */
  camera->m_LookAt = object->m_Position;
  camera->m_TargetID = objectID;
  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VECameraSetTarget' function */

/***
 * PURPOSE: Set camera distance to target object
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] distance - distance to target object
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraSetDistance( const VEUINT sceneID, const VEUINT cameraID, const VEREAL distance )
{
  VEOBJECT *object = NULL;
  VECAMERA *camera = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Get camera */
  camera = VEInternalContainerGet(scene->m_Cameras, cameraID);
  if (!camera)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Get object */
  object = VEInternalContainerGet(scene->m_Objects, camera->m_TargetID);
  if (!object)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Update camera distance */
  camera->m_Distance = distance;

  /* Update camera position */
  camera->m_Position.m_X = camera->m_LookAt.m_X + camera->m_Distance * sin(camera->m_Angles.m_X) * cos(camera->m_Angles.m_Y);
  camera->m_Position.m_Y = camera->m_LookAt.m_Y + camera->m_Distance * sin(camera->m_Angles.m_Y);
  camera->m_Position.m_Z = camera->m_LookAt.m_Z + camera->m_Distance * cos(camera->m_Angles.m_X) * cos(camera->m_Angles.m_Y);

  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VECameraSetDistance' function */

/***
 * PURPOSE: Rotates camera by yaw
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] yaw      - angle to rotate
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraRotateYaw( const VEUINT sceneID, const VEUINT cameraID, const VEREAL yaw )
{
  VEOBJECT *object = NULL;
  VECAMERA *camera = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Get camera */
  camera = VEInternalContainerGet(scene->m_Cameras, cameraID);
  if (!camera)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Yaw (XZ) rotation */
  camera->m_Angles.m_X += VE_DEGTORAD(yaw);

  /* Is camera free-flight? */
  object = VEInternalContainerGet(scene->m_Objects, camera->m_TargetID);
  if (!object)
  {
    VECameraUpdate(camera);
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Update camera position */
  camera->m_Position.m_X = camera->m_LookAt.m_X + camera->m_Distance * sin(camera->m_Angles.m_X) * cos(camera->m_Angles.m_Y);
  camera->m_Position.m_Z = camera->m_LookAt.m_Z + camera->m_Distance * cos(camera->m_Angles.m_X) * cos(camera->m_Angles.m_Y);

  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VECameraRotateYaw' function */

/***
 * PURPOSE: Rotates camera by pitch
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] pitch    - angle to rotate
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraRotatePitch( const VEUINT sceneID, const VEUINT cameraID, const VEREAL pitch )
{
  VEOBJECT *object = NULL;
  VECAMERA *camera = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Get camera */
  camera = VEInternalContainerGet(scene->m_Cameras, cameraID);
  if (!camera)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Update angle */
  camera->m_Angles.m_Y += VE_DEGTORAD(pitch);

  /* Recalculate angles */
  if (camera->m_Angles.m_Y > M_PI_2 - 0.04)
    camera->m_Angles.m_Y = M_PI_2 - 0.04;
  if (camera->m_Angles.m_Y < 0.04-M_PI_2)
    camera->m_Angles.m_Y = 0.04-M_PI_2;

  /* Is camera free-flight? */
  object = VEInternalContainerGet(scene->m_Objects, camera->m_TargetID);
  if (!object)
  {
    VECameraUpdate(camera);
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Update camera position */
  camera->m_Position.m_X = camera->m_LookAt.m_X + camera->m_Distance * sin(camera->m_Angles.m_X) * cos(camera->m_Angles.m_Y);
  camera->m_Position.m_Y = camera->m_LookAt.m_Y + camera->m_Distance * sin(camera->m_Angles.m_Y);
  camera->m_Position.m_Z = camera->m_LookAt.m_Z + camera->m_Distance * cos(camera->m_Angles.m_X) * cos(camera->m_Angles.m_Y);

  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VECameraRotateYaw' function */

/***
 * PURPOSE: Set camera angles
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] angles   - angles to set
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraSetAngles( const VEUINT sceneID, const VEUINT cameraID, const VEVECTOR2D angles )
{
  VECAMERA *camera = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Get camera */
  camera = VEInternalContainerGet(scene->m_Cameras, cameraID);
  if (!camera)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Setup angles */
  camera->m_Angles = angles;

  /* Get object */
  if (camera->m_TargetID == 0)
  {
    VECameraUpdate(camera);
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Update camera position */
  camera->m_Position.m_X = camera->m_LookAt.m_X + camera->m_Distance * sin(camera->m_Angles.m_X) * cos(camera->m_Angles.m_Y);
  camera->m_Position.m_Y = camera->m_LookAt.m_Y + camera->m_Distance * sin(camera->m_Angles.m_Y);
  camera->m_Position.m_Z = camera->m_LookAt.m_Z + camera->m_Distance * cos(camera->m_Angles.m_X) * cos(camera->m_Angles.m_Y);

  /* Setup angles */
  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VECameraSetAngles' function */

/***
 * PURPOSE: Set camera yaw
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] yaw      - yaw angle
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraSetAngleYaw( const VEUINT sceneID, const VEUINT cameraID, const VEREAL yaw )
{
  VECAMERA *camera = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Get camera */
  camera = VEInternalContainerGet(scene->m_Cameras, cameraID);
  if (!camera)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Setup angles */
  camera->m_Angles.m_X = yaw;

  /* Get object */
  if (camera->m_TargetID == 0)
  {
    VECameraUpdate(camera);
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VECameraSetAngleYaw' function */

/***
 * PURPOSE: Set camera pitch
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *   PARAM: [IN] pitch    - pitch angle
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraSetAnglePitch( const VEUINT sceneID, const VEUINT cameraID, const VEREAL pitch )
{
  VECAMERA *camera = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Get camera */
  camera = VEInternalContainerGet(scene->m_Cameras, cameraID);
  if (!camera)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Setup angles */
  camera->m_Angles.m_Y = pitch;

  /* Get object */
  if (camera->m_TargetID == 0)
  {
    VECameraUpdate(camera);
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VECameraSetAnglePitch' function */

/***
 * PURPOSE: Move camera along the direction
 *   PARAM: [IN] sceneID   - scene identifier
 *   PARAM: [IN] cameraID  - existing camera identifier
 *   PARAM: [IN] value     - distance to move
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraMove( const VEUINT sceneID, const VEUINT cameraID, const VEREAL value )
{
  VEVECTOR3D direction;
  VECAMERA *camera = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Get camera */
  camera = VEInternalContainerGet(scene->m_Cameras, cameraID);
  if (!camera)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Get object */
  if (camera->m_TargetID != 0)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Camera direction */
  direction = VEVector3DSub(camera->m_LookAt, camera->m_Position);

  /* Move camera */
  camera->m_Position = VEVector3DAdd(camera->m_Position, VEVector3DMult(direction, value));
  camera->m_LookAt   = VEVector3DAdd(camera->m_Position, direction);

  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VECameraMove' function */

/***
 * PURPOSE: Strafe camera (left-right)
 *   PARAM: [IN] sceneID   - scene identifier
 *   PARAM: [IN] cameraID  - existing camera identifier
 *   PARAM: [IN] value     - distance to move
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraStrafe( const VEUINT sceneID, const VEUINT cameraID, const VEREAL value )
{

} /* End of 'VECameraStrafe' function */

/***
 * PURPOSE: Get camera distance to target object
 *  RETURN: Curent camera distance to target object
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] cameraID - existing camera identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VECameraGetDistance( const VEUINT sceneID, const VEUINT cameraID )
{
  VEREAL distance = 0.0;
  VECAMERA *camera = NULL;
  VESCENE *scene = NULL;

  VESectionEnterInternal(p_SettingsGraphicsSection);

  scene = VESceneGet(sceneID);
  if (!scene)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return 0.0;
  }

  /* Get camera */
  camera = VEInternalContainerGet(scene->m_Cameras, cameraID);
  if (!camera)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return 0.0;
  }

  if (camera->m_TargetID != 0)
    distance = camera->m_Distance;

  VESectionLeaveInternal(p_SettingsGraphicsSection);
  return distance;
} /* End of 'VECameraGetDistance' function */

/***
 * PURPOSE: Update all attached cameras position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEUpdateCameras( VEVOID )
{
  VEOBJECT *object = NULL;
  VECAMERA *camera = NULL;
  VESCENE *scene = VESceneGet(p_SettingsGraphicsSceneID);
  if (!scene)
    return;

  /* Get active camera */
  camera = VEInternalContainerGet(scene->m_Cameras, scene->m_CameraID);
  if (!camera)
    return;

  /* Camera hasn't any target */
  if (camera->m_TargetID == 0)
    return;

  /* Get target object */
  object = VEInternalContainerGet(scene->m_Objects, camera->m_TargetID);
  if (!object)
  {
    VEConsolePrint("Object doesn't exist, camera detached");
    camera->m_TargetID = 0;
    return;
  }

  /* Update camera position */
  camera->m_LookAt = object->m_Position;
} /* End of 'VEUpdateCameras' function */

/***
 * PURPOSE: Print camera information to console
 *   PARAM: [IN] camera - camera to print information
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraPrint( VECAMERA *camera )
{
  VEVECTOR3D dir = VEVector3DNormalize(VEVector3DSub(camera->m_LookAt, camera->m_Position));
  VECHAR cameraInfo[VE_BUFFER_STANDART];
  memset(cameraInfo, 0, VE_BUFFER_STANDART);

  /* Print scene information to console */
  sprintf(cameraInfo, "ID: %d; Target ID: %d; Distance: %.2lf; Pos: [%.2lf; %.2lf; %.2lf]; Dir: [%.2lf; %.2lf; %.2lf]",
          camera->m_ID,
          camera->m_TargetID,
          camera->m_Distance,
          camera->m_Position.m_X,    camera->m_Position.m_Y,    camera->m_Position.m_Z,
          dir.m_X, dir.m_Y, dir.m_Z);

  VEConsolePrint(cameraInfo);
} /* End of 'VECameraPrint' function */

/***
 * PURPOSE: Print camera list to console
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraList( VEVOID )
{
  VESCENE *scene = VESceneGet(p_SettingsGraphicsSceneID);
  VEInternalContainerForeach((VEINTERNALCONTAINER*)scene->m_Cameras, (VEFUNCTION)VECameraPrint);
} /* End of 'VECameraList' function */

/***
 * PURPOSE: Render camera as cone
 *   PARAM: [IN] camera - pointer to light
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraRenderInternal( const VECAMERA *camera )
{
  glColor3f(1.0, 1.0, 0.0);
  glBegin(GL_LINES);
    glVertex3f(camera->m_Position.m_X, camera->m_Position.m_Y, camera->m_Position.m_Z);
    glVertex3f(camera->m_LookAt.m_X,   camera->m_LookAt.m_Y,   camera->m_LookAt.m_Z);
  glEnd();

  glPushMatrix();
  glLoadIdentity();

  /* Translate CS */
  glTranslatef(camera->m_Position.m_X, camera->m_Position.m_Y, camera->m_Position.m_Z);
  glutSolidSphere(0.1, 8, 8);

  glPopMatrix();
} /* End of 'VECameraRenderInternal' function */

/***
 * PURPOSE: Render all lights of scene as spheres
 *   PARAM: [IN] sceneID - scene identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VECameraRender( const VEUINT sceneID )
{
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
    return;

  /* Render all lights */
  VEInternalContainerForeach(scene->m_Cameras, (VEFUNCTION)VECameraRenderInternal);
} /* End of 'VELightRender' function */
