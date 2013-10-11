#ifndef INTERNALMENU_H_INCLUDED
#define INTERNALMENU_H_INCLUDED

#include "types.h"

/***
 * PURPOSE: Initialize menu module
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEMenuInit( VEVOID );

/***
 * PURPOSE: Remove all sub-menus, which doesn't contains any items
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMenuPrepare( VEVOID );

/***
 * PURPOSE: Deinitialize menu module
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMenuDeinit( VEVOID );

#endif // INTERNALMENU_H_INCLUDED
