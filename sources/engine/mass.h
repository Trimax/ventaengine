#ifndef MASS_H_INCLUDED
#define MASS_H_INCLUDED

#include "types.h"

/*** Constants ***/

/* Gravitation constant */
#define VE_PHYSICS_G 6.67428e-11

/* Velocity of the ligth */
#define VE_PHYSICS_LIGHT_VELOCITY 299792458

/***
 * PURPOSE: Set object mass (kg)
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier
 *   PARAM: [IN] mass     - new object's mass
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectSetMass( const VEUINT sceneID, const VEUINT objectID, const VEREAL mass );

/***
 * PURPOSE: Get object mass (kg)
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier
  *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEObjectGetMass( const VEUINT sceneID, const VEUINT objectID );


#endif // MASS_H_INCLUDED
