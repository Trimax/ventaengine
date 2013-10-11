#ifndef INTERNALBUTTON_H_INCLUDED
#define INTERNALBUTTON_H_INCLUDED

#include "internalstring.h"
#include "physics.h"
#include "math.h"

/*** Button states ***/

/* Normal button */
#define VE_BUTTON_NORMAL    1

/* Hovered button */
#define VE_BUTTON_HOVERED   2

/* Clicked button */
#define VE_BUTTON_CLICKED   3

/* Disabled button */
#define VE_BUTTON_DISABLED  4

/* Button structure */
typedef struct tagVEWIDGETBUTTON
{
  VEVECTOR4D  m_Color;      /* Button background color */
  VEVECTOR4D  m_TextColor;  /* Button text color */
  VESTRING   *m_Text;       /* Button text */
  VEBYTE      m_State;      /* Current button state (one of VE_BUTTON_XXX definitions) */
} VEWIDGETBUTTON;

/**
 * PURPOSE: Render button widget
 *   PARAM: [IN] widget   - pointer to button
 *   PARAM: [IN] position - absolute widget position
 *   PARAM: [IN] size     - widget size
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWidgetButtonRenderInternal( VEWIDGETBUTTON *widget, VEVECTOR2D position, VEVECTOR2D size );

/**
 * PURPOSE: Delete button widget
 *   PARAM: [IN] widget - pointer to button
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWidgetButtonDeleteInternal( VEWIDGETBUTTON *widget );

/**
 * PURPOSE: Inject attribute to widget
 *   PARAM: [IN] widget - pointer to button
 *   PARAM: [IN] name   - attribute name
 *   PARAM: [IN] value  - attribute value
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWidgetButtonInjectAttributeInternal( VEWIDGETBUTTON *widget, VEBUFFER name, VEBUFFER value );

#endif // INTERNALBUTTON_H_INCLUDED
