#include "internalglut.h"

#include "internalcontainer.h"
#include "memorymanager.h"
#include "internalmenu.h"
#include "types.h"
#include "menu.h"

#include <string.h>

/* Menus */
volatile static VEINTERNALCONTAINER *p_MenuItemsContainer = NULL;
volatile static VEINTERNALCONTAINER *p_MenuContainer      = NULL;
static VEINT mainMenu = 0;

/* Menu item structure */
typedef struct tagVEMENUITEM
{
  VEINT       m_ID;        /* Self identifier */
  VEPROCEDURE m_Processor; /* Related processor */
} VEMENUITEM;

/* Menu structure */
typedef struct tagVEMENU
{
  VEINT  m_ID;       /* Menu identifier */
  VEUINT m_NumMenus; /* The number of sub menus */
  VEUINT m_NumItems; /* The number of items */
} VEMENU;

/***
 * PURPOSE: Menu processing
 *   PARAM: [IN] menuID - selected menu item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMenuProcessor( VEINT menuID )
{
  VEMENUITEM *item = VEInternalContainerGet((VEINTERNALCONTAINER*)p_MenuItemsContainer, menuID);
  if (!item)
    return;

  /* Processor wasn't specified */
  if (!item->m_Processor)
    return;

  /* Execute processor function */
  item->m_Processor();
} /* End of 'VEMenuProcessor' function */

/***
 * PURPOSE: Initialize menu module
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEMenuInit( VEVOID )
{
  if (p_MenuContainer)
    return TRUE;
  if (p_MenuItemsContainer)
    return TRUE;

  /* Menu container creation */
  p_MenuContainer = VEInternalContainerCreate(VE_DEFAULT_CONTAINERSIZE, "Menu container");
  if (!p_MenuContainer)
    return FALSE;

  /* Menu items container creation */
  p_MenuItemsContainer = VEInternalContainerCreate(VE_DEFAULT_CONTAINERSIZE, "Menu items container");
  if (!p_MenuItemsContainer)
  {
    VEInternalContainerDelete((VEINTERNALCONTAINER*)p_MenuContainer);
    p_MenuContainer = NULL;
    return FALSE;
  }

  /* Main menu creation */
  mainMenu    = glutCreateMenu(VEMenuProcessor);
  return TRUE;
} /* End if 'VEMenuInit' function */

VEVOID VEMenuItemDeleteInternal( VEMENUITEM *item )
{
  Delete(item);
} /* End of 'VEMenuItemDeleteInternal' function */

/***
 * PURPOSE: Deinitialize menu module
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMenuDeinit( VEVOID )
{
  VEInternalContainerForeach((VEINTERNALCONTAINER*)p_MenuItemsContainer, (VEFUNCTION)VEMenuItemDeleteInternal);
  VEInternalContainerDelete((VEINTERNALCONTAINER*)p_MenuItemsContainer);
  p_MenuItemsContainer = NULL;

  VEInternalContainerDelete((VEINTERNALCONTAINER*)p_MenuContainer);
  p_MenuContainer = NULL;

  if (mainMenu)
    glutDestroyMenu(mainMenu);
  mainMenu    = 0;
} /* End if 'VEMenuInit' function */

/***
 * PURPOSE: Create sub-menu
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] parentID - parent menu identifier (0 if you want to add submenu to main menu)
 *   PARAM: [IN] menuID   - created menu GLUT identifier
 *   PARAM: [IN] name     - submenu name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEMenuCreateInternal( const VEUINT parentID, const VEUINT menuID, const VEBUFFER name )
{
  if (parentID == 0)
    glutSetMenu(mainMenu);
  else
  {
    VEMENU *parentMenu = VEInternalContainerGet((VEINTERNALCONTAINER*)p_MenuContainer, parentID);
    if (!parentMenu)
      return FALSE;

    /* Update parent menu information */
    glutSetMenu(parentMenu->m_ID);
    parentMenu->m_NumMenus++;
  }

  /* Adding submenu */
  glutAddSubMenu(name, menuID);

  /* That's it */
  glutSetMenu(mainMenu);
  return TRUE;
} /* End of 'VEMenuCreateInternal' function */

/***
 * PURPOSE: Create new sub-menu
 *  RETURN: Created menu identifier if success, 0 otherwise
 *   PARAM: [IN] parentID - parent menu identifier (0 if you want to add submenu to main menu)
 *   PARAM: [IN] name     - submenu name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEMenuCreate( const VEUINT parentID, const VEBUFFER name )
{
  VEMENU *menu = NULL;

  /* Manager wasn't initialized */
  if (!mainMenu)
    return 0;

  /* Wrong item name */
  if (!name)
    return 0;

  /* Menu creation */
  menu = New(sizeof(VEMENU), "Menu");
  if (!menu)
    return 0;

  /* Real menu creation */
  menu->m_ID = glutCreateMenu(VEMenuProcessor);

  /* Select menu to add submenu */
  if (!VEMenuCreateInternal(parentID, menu->m_ID, name))
  {
    glutDestroyMenu(menu->m_ID);
    Delete(menu);
    return 0;
  }

  /* That's it */
  glutSetMenu(mainMenu);
  return VEInternalContainerAdd((VEINTERNALCONTAINER*)p_MenuContainer, menu);;
} /* End of 'VEMenuCreate' function */

/***
 * PURPOSE: Add item to context menu
 *  RETURN: Created menu item if success, 0 otherwise
 *   PARAM: [IN] menuID    - menu identifier
 *   PARAM: [IN] itemID    - menu item identifier
 *   PARAM: [IN] name      - menu item name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEMenuItemCreateInternal( const VEUINT menuID, const VEUINT itemID, const VEBUFFER name )
{
  if (menuID == 0)
    glutSetMenu(mainMenu);
  else
  {
    VEMENU *menu = VEInternalContainerGet((VEINTERNALCONTAINER*)p_MenuContainer, menuID);
    if (!menu)
      return FALSE;

    /* Update parent menu information */
    glutSetMenu(menu->m_ID);
    menu->m_NumItems++;
  }

  /* Adding menu item */
  glutAddMenuEntry(name, itemID);

  /* That's it */
  glutSetMenu(mainMenu);
  return TRUE;
} /* End of 'VEMenuItemCreateInternal' function */

/***
 * PURPOSE: Add item to context menu
 *  RETURN: Created menu item if success, 0 otherwise
 *   PARAM: [IN] menuID    - menu identifier
 *   PARAM: [IN] name      - menu item name
 *   PARAM: [IN] processor - menu item processor
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEMenuItemCreate( const VEINT menuID, const VEBUFFER name, VEPROCEDURE processor )
{
  VEMENUITEM *item = NULL;

  /* Manager wasn't initialized */
  if (!mainMenu)
    return 0;

  /* Wrong item name */
  if (!name)
    return 0;

  /* Menu item allocation */
  item = New(sizeof(VEMENUITEM), "Menu item creation");
  if (!item)
    return 0;

  /* Store item processor */
  item->m_Processor = processor;
  item->m_ID        = VEInternalContainerAdd((VEINTERNALCONTAINER*)p_MenuItemsContainer, item);

  /* Real menu item creation */
  if (!VEMenuItemCreateInternal(menuID, item->m_ID, name))
  {
    VEInternalContainerRemove((VEINTERNALCONTAINER*)p_MenuItemsContainer, item->m_ID);
    Delete(item);
    return 0;
  }

  /* That's it */
  glutSetMenu(mainMenu);
  glutAttachMenu(GLUT_RIGHT_BUTTON);
  return item->m_ID;
} /* End of 'VEMenuAddItem' function */

VEBOOL VEMenuCheck( VEMENU *menu )
{
  if (!menu)
    return TRUE;

  /* Empty sub menu should be removed */
  if ((menu->m_NumMenus + menu->m_NumItems) == 0)
  {
    glutDestroyMenu(menu->m_ID);
    Delete(menu);
  }

  /* It is normal menu */
  return FALSE;
} /* End of 'VEMenuCheck' function */

/***
 * PURPOSE: Remove all sub-menus, which doesn't contains any items
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMenuPrepare( VEVOID )
{
  VEInternalContainerFilter((VEINTERNALCONTAINER*)p_MenuContainer, (VEFUNCTIONFILTER)VEMenuCheck);
} /* End of 'VEMenuPrepare' function */
