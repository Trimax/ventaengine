#include "internalglut.h"

#include "internaldialogmanager.h"
#include "internalbuffer.h"
#include "internalbutton.h"
#include "memorymanager.h"
#include "internaltext.h"

/**
 * PURPOSE: Render button widget
 *   PARAM: [IN] widget   - pointer to button
 *   PARAM: [IN] position - absolute widget position
 *   PARAM: [IN] size     - widget size
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWidgetButtonRenderInternal( VEWIDGETBUTTON *widget, VEVECTOR2D position, VEVECTOR2D size )
{
  /* Draw button borders */
  VEVECTOR4D lightBorder = VEVector4DMult(widget->m_Color, 1.5);
  VEVECTOR4D darkBorder  = VEVector4DMult(widget->m_Color, 0.5);

  /* Button background */
  glBegin(GL_QUADS);
    glColor4f(widget->m_Color.m_X, widget->m_Color.m_Y, widget->m_Color.m_Z, widget->m_Color.m_W);

    glVertex2i(position.m_X,            position.m_Y);
    glVertex2i(position.m_X + size.m_X, position.m_Y);
    glVertex2i(position.m_X + size.m_X, position.m_Y + size.m_Y);
    glVertex2i(position.m_X,            position.m_Y + size.m_Y);
  glEnd();

  /* Window border */
  glBegin(GL_LINES);

    /* Light border */
    glColor4f(lightBorder.m_X, lightBorder.m_Y, lightBorder.m_Z, widget->m_Color.m_W);
    glVertex2i(position.m_X,            position.m_Y);
    glVertex2i(position.m_X + size.m_X, position.m_Y);
    glVertex2i(position.m_X,            position.m_Y);
    glVertex2i(position.m_X,            position.m_Y + size.m_Y);

    /* Dark border */
    glColor4f(darkBorder.m_X,  darkBorder.m_Y,  darkBorder.m_Z,  widget->m_Color.m_W);
    glVertex2i(position.m_X + size.m_X, position.m_Y);
    glVertex2i(position.m_X + size.m_X, position.m_Y + size.m_Y);
    glVertex2i(position.m_X + size.m_X, position.m_Y + size.m_Y);
    glVertex2i(position.m_X,            position.m_Y + size.m_Y);
  glEnd();

  /* Draw button title */
  if (widget->m_Text)
  {
    VEGraphicsPrintUsingVector(position.m_X + (size.m_X - (VEREAL)widget->m_Text->m_Size * VE_FONT_SYMBOL_WIDTH) * 0.5, position.m_Y + size.m_Y * 0.5 + VE_FONT_SYMBOL_HEIGHT / 3, widget->m_TextColor, widget->m_Text->m_Data);
  }
} /* End of 'VEWidgetButtonRenderInternal' function */

/**
 * PURPOSE: Delete button widget
 *   PARAM: [IN] widget - pointer to button
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWidgetButtonDeleteInternal( VEWIDGETBUTTON *widget )
{
  VEStringDeleteInternal(widget->m_Text);
  Delete(widget);
} /* End of 'VEWidgetButtonDeleteInternal' function */

/**
 * PURPOSE: Inject attribute to widget
 *   PARAM: [IN] widget - pointer to button
 *   PARAM: [IN] name   - attribute name
 *   PARAM: [IN] value  - attribute value
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWidgetButtonInjectAttributeInternal( VEWIDGETBUTTON *widget, VEBUFFER name, VEBUFFER value )
{
  if (VEBuffersEqual(name, VE_PARAM_TITLE)) {
    VEStringDeleteInternal(widget->m_Text);
    widget->m_Text = VEStringCreateFromBufferInternal(value);
  } else if (VEBuffersEqual(name, VE_PARAM_COLOR))
    widget->m_Color = VEColorToVector4D(VEBufferToColor(value));
  else if (VEBuffersEqual(name, VE_PARAM_COLORTEXT))
    widget->m_TextColor = VEColorToVector4D(VEBufferToColor(value));
} /* End of 'VEWidgetButtonInjectAttributeInternal' function */
