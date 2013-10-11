#ifndef SKINMANAGER_H_INCLUDED
#define SKINMANAGER_H_INCLUDED

#include "types.h"

/***
 * PURPOSE: Load skin data
 *  RETURN: Loaded skin identifier if success, 0 otherwise
 *   PARAM: [IN] filename - skin file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEGUISkinLoad( const VEBUFFER filename );

/***
 * PURPOSE: Apply skin to entire GUI
 *   PARAM: [IN] skinID - loaded skin identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGUISkinApply( const VEUINT skinID );

/***
 * PURPOSE: Unload skin data
 *   PARAM: [IN] skinID - loaded skin identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGUISkinUnload( const VEUINT skinID );


#endif // SKINMANAGER_H_INCLUDED
