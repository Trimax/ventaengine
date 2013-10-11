#include "internaldialogmanager.h"
#include "internalstring.h"
#include "internalbuffer.h"
#include "memorymanager.h"
#include "internallabel.h"
#include "internaltext.h"

/**
 * PURPOSE: Render label widget
 *   PARAM: [IN] widget   - pointer to label
 *   PARAM: [IN] position - absolute widget position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWidgetLabelRenderInternal( VEWIDGETLABEL *widget, VEVECTOR2D position )
{
  if (widget->m_Text)
    VEGraphicsPrintUsingVector(position.m_X, position.m_Y, widget->m_TextColor, widget->m_Text->m_Data);
} /* End of 'VEWidgetLabelRenderInternal' function */

/**
 * PURPOSE: Delete label widget
 *   PARAM: [IN] widget - pointer to label
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWidgetLabelDeleteInternal( VEWIDGETLABEL *widget )
{
  VEStringDeleteInternal(widget->m_Text);
  Delete(widget);
} /* End of 'VEWidgetLabelDeleteInternal' function */

/**
 * PURPOSE: Inject attribute to widget
 *   PARAM: [IN] widget - pointer to label
 *   PARAM: [IN] name   - attribute name
 *   PARAM: [IN] value  - attribute value
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWidgetLabelInjectAttributeInternal( VEWIDGETLABEL *widget, VEBUFFER name, VEBUFFER value )
{
  if (VEBuffersEqual(name, VE_PARAM_TITLE)) {
    VEStringDeleteInternal(widget->m_Text);
    widget->m_Text = VEStringCreateFromBufferInternal(value);
  } else if (VEBuffersEqual(name, VE_PARAM_COLORTEXT))
    widget->m_TextColor = VEColorToVector4D(VEBufferToColor(value));
} /* End of 'VEWidgetLabelInjectAttributeInternal' function */
