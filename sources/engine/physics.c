#include "internalscenemanager.h"
#include "internalcontainer.h"
#include "internalphysics.h"
#include "internalobject.h"
#include "temperature.h"
#include "console.h"
#include "charge.h"
#include "mass.h"
#include "math.h"

#include <math.h>

/***
 * PURPOSE: Set object temperature (K)
 *   PARAM: [IN] sceneID     - scene identifier where is object
 *   PARAM: [IN] objectID    - object identifier
 *   PARAM: [IN] temperature - new object's temperature
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectSetTemperature( const VEUINT sceneID, const VEUINT objectID, const VEREAL temperature )
{
  VEOBJECT *object = NULL;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return;
  }

  /* Remove object from scene */
  object = VEInternalContainerGet(scene->m_Objects, objectID);
  if (object)
    object->m_Temperature = temperature;
  else
    VEConsolePrint("Object doesn't exist");
} /* End of 'VEObjectSetTemperature' function */

/***
 * PURPOSE: Get object temperature (K)
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier
  *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEObjectGetTemperature( const VEUINT sceneID, const VEUINT objectID )
{
  VEOBJECT *object = NULL;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return 0.0;
  }

  /* Remove object from scene */
  object = VEInternalContainerGet(scene->m_Objects, objectID);
  if (object)
    return object->m_Temperature;
  else
    VEConsolePrint("Object doesn't exist");
  return 0.0;
} /* End of 'VEObjectGetTemperature' function */

/***
 * PURPOSE: Set object mass (kg)
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier
 *   PARAM: [IN] mass     - new object's mass
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectSetMass( const VEUINT sceneID, const VEUINT objectID, const VEREAL mass )
{
  VEOBJECT *object = NULL;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return;
  }

  /* Remove object from scene */
  object = VEInternalContainerGet(scene->m_Objects, objectID);
  if (object)
  {
    object->m_Mass = mass;
    object->m_EventHorizont = VEMAX(1.0, VE_PHYSICS_G * mass / pow(VE_PHYSICS_LIGHT_VELOCITY, 2.0));
  }
  else
    VEConsolePrint("Object doesn't exist");
} /* End of 'VEObjectSetMass' function */

/***
 * PURPOSE: Get object mass (kg)
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier
  *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEObjectGetMass( const VEUINT sceneID, const VEUINT objectID )
{
  VEOBJECT *object = NULL;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return 0.0;
  }

  /* Remove object from scene */
  object = VEInternalContainerGet(scene->m_Objects, objectID);
  if (object)
    return object->m_Mass;
  else
    VEConsolePrint("Object doesn't exist");
  return 0.0;
} /* End of 'VEObjectGetMass' function */

/***
 * PURPOSE: Set object charge (Kl)
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier
 *   PARAM: [IN] charge   - new object's charge
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectSetCharge( const VEUINT sceneID, const VEUINT objectID, const VEREAL charge )
{
  VEOBJECT *object = NULL;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return;
  }

  /* Remove object from scene */
  object = VEInternalContainerGet(scene->m_Objects, objectID);
  if (object)
    object->m_Charge = charge;
  else
    VEConsolePrint("Object doesn't exist");
} /* End of 'VEObjectSetCharge' function */

/***
 * PURPOSE: Get object charge (Kl)
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier
  *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEObjectGetCharge( const VEUINT sceneID, const VEUINT objectID )
{
  VEOBJECT *object = NULL;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return 0.0;
  }

  /* Remove object from scene */
  object = VEInternalContainerGet(scene->m_Objects, objectID);
  if (object)
    return object->m_Charge;
  else
    VEConsolePrint("Object doesn't exist");
  return 0.0;
} /* End of 'VEObjectGetCharge' function */
