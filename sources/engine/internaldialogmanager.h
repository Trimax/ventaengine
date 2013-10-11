#ifndef INTERNALDIALOGMANAGER_H_INCLUDED
#define INTERNALDIALOGMANAGER_H_INCLUDED

#include "internalcontainer.h"
#include "internalrectangle.h"
#include "internalwidget.h"
#include "physics.h"

/*** Window styles ***/

/* Simple rectangle */
#define VE_WINDOW_STYLE_SIMPLE 0

/* Rectangle with header */
#define VE_WINDOW_STYLE_HEADER 1

/*** Special symbols ***/

/* Commented line */
#define VE_SYMBOL_COMMENT    '#'

/* Child start tag */
#define VE_SYMBOL_CHILDBEGIN '{'

/* Child start tag */
#define VE_SYMBOL_CHILDEND   '}'

/* Array start tag */
#define VE_SYMBOL_ARRAYBEGIN '['

/* Array end tag */
#define VE_SYMBOL_ARRAYEND   ']'

/* Items separator */
#define VE_SYMBOL_SEPARATOR  ','

/*** Known parameters ***/

/* Window/widget ID */
#define VE_PARAM_ID        "id"

/* Window caption */
#define VE_PARAM_TITLE     "title"

/* Window/widget width */
#define VE_PARAM_WIDTH     "width"

/* Window/widget height */
#define VE_PARAM_HEIGHT    "height"

/* Window/widget background color*/
#define VE_PARAM_COLOR     "color"

/* Window/widget text color*/
#define VE_PARAM_COLORTEXT "textcolor"

/* Window/widget position x */
#define VE_PARAM_X         "x"

/* Window/widget position y */
#define VE_PARAM_Y         "y"

/* Window structure */
typedef struct tagVEWINDOW
{
  VESTRING            *m_ID;          /* Window identifier */
  VESTRING            *m_Title;       /* Window title */
  VERECTANGLE          m_Dimensions;  /* Window dimensions */
  VEVECTOR4D           m_Color;       /* Window background color */
  VEINTERNALCONTAINER *m_Widgets;     /* Child widgets */
  VEBYTE               m_IsMoveable;  /* Is window moveable */
  VEBYTE               m_IsVisible;   /* Is window visible */
} VEWINDOW;

/***
 * PURPOSE: Load window from file
 *  RETURN: Pointer to loaded window if success, NULL otherwise
 *   PARAM: [IN] filename - file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEWINDOW *VEWindowLoadInternal( const VEBUFFER filename );

/***
 * PURPOSE: Deletes window
 *   PARAM: [IN] window - pointer to window to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWindowDeleteInternal( VEWINDOW *window );

#endif // INTERNALDIALOGMANAGER_H_INCLUDED
