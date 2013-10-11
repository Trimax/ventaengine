#ifndef MENU_H_INCLUDED
#define MENU_H_INCLUDED

#include "types.h"

/*** Definitions ***/

/* Main menu identifier */
#define VE_MENU_MAIN 0

/***
 * PURPOSE: Create new sub-menu
 *  RETURN: Created menu identifier if success, 0 otherwise
 *   PARAM: [IN] parentID - parent menu identifier (VE_MENU_MAIN if you want to add submenu to main menu)
 *   PARAM: [IN] name     - submenu name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEMenuCreate( const VEUINT parentID, const VEBUFFER name );

/***
 * PURPOSE: Add item to context menu
 *  RETURN: Created menu item if success, 0 otherwise
 *   PARAM: [IN] menuID    - menu identifier (VE_MENU_MAIN if you want to add item to main menu)
 *   PARAM: [IN] name      - menu item name
 *   PARAM: [IN] processor - menu item processor
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEMenuItemCreate( const VEINT menuID, const VEBUFFER name, VEPROCEDURE processor );

#endif // MENU_H_INCLUDED
