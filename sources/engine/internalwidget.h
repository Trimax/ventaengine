#ifndef INTERNALWIDGET_H_INCLUDED
#define INTERNALWIDGET_H_INCLUDED

#include "internalrectangle.h"
#include "interfacemanager.h"
#include "internalstring.h"
#include "internalbutton.h"
#include "internallabel.h"
#include "physics.h"

/*** Widget types ***/

/* Label */
#define VE_WIDGET_LABEL     1
#define VE_WIDGETSTR_LABEL  "label"

/* Image */
#define VE_WIDGET_IMAGE     2
#define VE_WIDGETSTR_IMAGE  "image"

/* Button */
#define VE_WIDGET_BUTTON    3
#define VE_WIDGETSTR_BUTTON "button"

/* Edit */
#define VE_WIDGET_EDIT      4
#define VE_WIDGETSTR_EDIT   "label"

/* Widget structure (elementary item) */
typedef struct tagVEWIDGET
{
  VERECTANGLE      m_Dimensions;  /* Widget dimensions */
  VEPOINTER       *m_Control;     /* Pointer to control */
  VESTRING        *m_ID;          /* Widget identifier */
  VEBYTE           m_Type;        /* Widget type (see VE_WIDGET_XXX definitions) */
  VEBYTE           m_IsVisible;   /* Visibility flag */
  VEEVENTPROCESSOR m_Processor;   /* Widget processor */
} VEWIDGET;

#endif // INTERNALWIDGET_H_INCLUDED
