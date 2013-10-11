#ifndef INTERNALLABEL_H_INCLUDED
#define INTERNALLABEL_H_INCLUDED

#include "internalstring.h"
#include "physics.h"
#include "math.h"

/* Label structure */
typedef struct tagVEWIDGETLABEL
{
  VEVECTOR4D m_TextColor;  /* Label text color */
  VESTRING  *m_Text;       /* Label text */
} VEWIDGETLABEL;

/**
 * PURPOSE: Render label widget
 *   PARAM: [IN] widget   - pointer to label
 *   PARAM: [IN] position - absolute widget position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWidgetLabelRenderInternal( VEWIDGETLABEL *widget, VEVECTOR2D position );

/**
 * PURPOSE: Delete label widget
 *   PARAM: [IN] widget - pointer to label
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWidgetLabelDeleteInternal( VEWIDGETLABEL *widget );

/**
 * PURPOSE: Inject attribute to widget
 *   PARAM: [IN] widget - pointer to label
 *   PARAM: [IN] name   - attribute name
 *   PARAM: [IN] value  - attribute value
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWidgetLabelInjectAttributeInternal( VEWIDGETLABEL *widget, VEBUFFER name, VEBUFFER value );

#endif // INTERNALLABEL_H_INCLUDED
