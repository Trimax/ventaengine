#ifndef RESOURCEMANAGER_H_INCLUDED
#define RESOURCEMANAGER_H_INCLUDED

#include "types.h"

/***
 * PURPOSE: Register resources path
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] name - resource group name
 *   PARAM: [IN] path - relative path to resources on HDD (root is working directory)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEResourceRegisterPath( const VEBUFFER name, const VEBUFFER path );

/***
 * PURPOSE: Register resources archive
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] name     - resource group name
 *   PARAM: [IN] filename - resource archive file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEResourceRegisterArchive( const VEBUFFER name, const VEBUFFER filename );

/***
 * PURPOSE: Unload resource group
 *   PARAM: [IN] name - resource group name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEResourceUnregister( const VEBUFFER name );

#endif // RESOURCEMANAGER_H_INCLUDED
