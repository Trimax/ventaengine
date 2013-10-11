#ifndef CHARGE_H_INCLUDED
#define CHARGE_H_INCLUDED

#include "types.h"

/***
 * PURPOSE: Set object charge (Kl)
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier
 *   PARAM: [IN] charge   - new object's charge
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectSetCharge( const VEUINT sceneID, const VEUINT objectID, const VEREAL charge );

/***
 * PURPOSE: Get object charge (Kl)
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier
  *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEObjectGetCharge( const VEUINT sceneID, const VEUINT objectID );

#endif // CHARGE_H_INCLUDED
