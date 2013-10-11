#ifndef TEMPERATURE_H_INCLUDED
#define TEMPERATURE_H_INCLUDED

#include "types.h"

/***
 * PURPOSE: Set object temperature (K)
 *   PARAM: [IN] sceneID     - scene identifier where is object
 *   PARAM: [IN] objectID    - object identifier
 *   PARAM: [IN] temperature - new object's temperature
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectSetTemperature( const VEUINT sceneID, const VEUINT objectID, const VEREAL temperature );

/***
 * PURPOSE: Get object temperature (K)
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier
  *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEObjectGetTemperature( const VEUINT sceneID, const VEUINT objectID );

#endif // TEMPERATURE_H_INCLUDED
