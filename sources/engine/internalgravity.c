#include "internalscenemanager.h"
#include "internalsettings.h"
#include "internalphysics.h"
#include "console.h"
#include "physics.h"
#include "math.h"

/***
 * Object's event horizont H = max(BBOUND_SPHERE.RADIUS, GM / C^2)
 ***/

/***
 * PURPOSE: Determine gravity force scalar value
 *  RETURN: Gravity force scalar value
 *   PARAM: [IN] object1 - object 1 pointer
 *   PARAM: [IN] object2 - object 2 pointer
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEPhysicsGetGravityForceValue( const VEOBJECT *object1, const VEOBJECT *object2 )
{
  VEREAL distance = VEVector3DDistanceSquare(object1->m_Position, object2->m_Position);
  return VE_PHYSICS_G * object1->m_Mass * object2->m_Mass / distance;
} /* End of 'VEPhysicsGetGravityForceValue' function */

/***
 * PURPOSE: Determine gravity force
 *  RETURN: Gravity force vector value
 *   PARAM: [IN] object1 - object 1 pointer
 *   PARAM: [IN] object2 - object 2 pointer
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEPhysicsGetGravityForce( const VEOBJECT *object1, const VEOBJECT *object2 )
{
  VEREAL forceValue    = VEPhysicsGetGravityForceValue(object1, object2);
  VEVECTOR3D direction = VEVector3DNormalize(VEVector3DSub(object2->m_Position, object1->m_Position));
  return VEVector3DMult(direction, forceValue);
} /* End of 'VEPhysicsGetGravityForce' function */

/***
 * PURPOSE: Update objects position according to gravitation
 *   PARAM: [IN] timeElapsed - time since last update
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEApplyGravity( const VEREAL timeElapsed )
{
  VEUINT numIterations = (VEUINT)VEMAX(timeElapsed / VE_FRAME_DURATION, 1), iteration = 0;
  VESCENE *scene = VESceneGet(p_SettingsGraphicsSceneID);
  if (!scene)
    return;

  /* Enter to the objects critical section */
  VESectionEnterInternal(scene->m_Objects->m_Sync);

  /* Simulate gravitation */
  for (iteration = 0; iteration < numIterations; iteration++)
  {
    VEPOINTER *objects = scene->m_Objects->m_Items;
    VEUINT objectID = 0, otherObjectID = 0;

    /* Calculate forces */
    for (objectID = 0; objectID < scene->m_Objects->m_Size; objectID++)
      if (objects[objectID])
      {
        VEOBJECT *currentObject = (VEOBJECT*)objects[objectID];
        currentObject->m_Force = VEVector3D(0.0, 0.0, 0.0);

        /* Calculate gravity force */
        for (otherObjectID = 0; otherObjectID < scene->m_Objects->m_Size; otherObjectID++)
          if (otherObjectID != objectID)
            if (objects[otherObjectID])
            {
              VEOBJECT *otherObject = (VEOBJECT*)objects[otherObjectID];

              if (!VEObjectsIntersects(currentObject, otherObject))
              {
                currentObject->m_Force = VEVector3DAdd(currentObject->m_Force, VEPhysicsGetGravityForce(currentObject, otherObject));
              } else {
                VEREAL k = currentObject->m_Mass / (currentObject->m_Mass + otherObject->m_Mass);
                VEVECTOR3D v = VEVector3DMix(currentObject->m_VelPosition, otherObject->m_VelPosition, k);

                currentObject->m_VelPosition = v;
                otherObject->m_VelPosition   = v;
              }
            }
      }

      /* Apply forces */
      for (objectID = 0; objectID < scene->m_Objects->m_Size; objectID++)
        if (objects[objectID])
        {
          VEOBJECT *currentObject = (VEOBJECT*)objects[objectID];
          if (currentObject->m_Mass > 0.001)
          {
            VEVECTOR3D axeleration = VEVector3DMult(currentObject->m_Force, VE_FRAME_DT / currentObject->m_Mass);

            /* Change object velocity */
            currentObject->m_VelPosition = VEVector3DAdd(currentObject->m_VelPosition, axeleration);

            /* Change object position */
            currentObject->m_Position = VEVector3DAdd(currentObject->m_Position, VEVector3DMult(currentObject->m_VelPosition, VE_FRAME_DT));
          }
        }
  }

#if 0
  {
    VEPOINTER *objects = scene->m_Objects->m_Items;
    VEOBJECT *asteroid = (VEOBJECT*)objects[2];
    char test[234];

    sprintf(test, "dt = %.4lf; f = [%.3lf %.3lf %.3lf]]; p = [%.3lf %.3lf %.3lf]; v = [%.3f %.3f %.3f]; mass = %.4f", timeElapsed,
                                                                                       asteroid->m_Force.m_X,       asteroid->m_Force.m_Y,       asteroid->m_Force.m_Z,
                                                                                       asteroid->m_Position.m_X,    asteroid->m_Position.m_Y,    asteroid->m_Position.m_Z,
                                                                                       asteroid->m_VelPosition.m_X, asteroid->m_VelPosition.m_Y, asteroid->m_VelPosition.m_Z,
                                                                                       asteroid->m_Mass);


    VEConsolePrint(test);
  }
  #endif

  VESectionLeaveInternal(scene->m_Objects->m_Sync);
} /* End of 'VEApplyGravity' function */
