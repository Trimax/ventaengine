#include "internalglut.h"

#include "internalcriticalsection.h"
#include "internaldialogmanager.h"
#include "internalcontainer.h"
#include "internalgraphics.h"
#include "interfacemanager.h"
#include "internalsettings.h"
#include "internalbuffer.h"
#include "memorymanager.h"
#include "internallist.h"
#include "internaltext.h"
#include "console.h"
#include "types.h"

/*** Window definitions ***/

/* Window header height */
#define VE_WINDOW_HEADER_HEIGHT 19

/* Interfaces container */
volatile static VEINTERNALCONTAINER *p_InterfacesContainer = NULL;

/* Interface structure */
typedef struct tagVEINTERFACE
{
  VEINTERNALCONTAINER *m_Windows;
} VEINTERFACE;

/***
 * PURPOSE: Initialize interfaces manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEInterfacesInit( VEVOID )
{
  /* Already initialized */
  if (p_InterfacesContainer)
    return TRUE;

  p_InterfacesContainer = VEInternalContainerCreate(VE_DEFAULT_CONTAINERSIZE, "Interfaces container");
  if (!p_InterfacesContainer)
    return FALSE;
  return TRUE;
} /* End of 'VEInterfacesInit' function */

/***
 * PURPOSE: Delete interface
 *   PARAM: [IN] interface - pointer to interface to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfaceDeleteInternal( VEINTERFACE *ifs )
{
  if (!ifs)
    return;

  VEInternalContainerForeach(ifs->m_Windows, (VEFUNCTION)VEWindowDeleteInternal);
  VEInternalContainerDelete((VEINTERNALCONTAINER*)ifs->m_Windows);

  /* Release memory */
  Delete(ifs);
} /* End of 'VEInterfaceDeleteInternal' function */

/***
 * PURPOSE: Deinitialize interfaces manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfacesDeinit( VEVOID )
{
  if (p_InterfacesContainer)
  {
    VEInternalContainerForeach((VEINTERNALCONTAINER*)p_InterfacesContainer, (VEFUNCTION)VEInterfaceDeleteInternal);
    VEInternalContainerDelete((VEINTERNALCONTAINER*)p_InterfacesContainer);
  }

  p_InterfacesContainer = NULL;
} /* End of 'VEInterfacesDeinit' function */

/***
 * PURPOSE: Delete existing interface
 *   PARAM: [IN] interfaceID - interface identifier to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfaceDelete( const VEUINT interfaceID )
{
  VEINTERFACE *ifs = NULL;
  VESectionEnterInternal(p_SettingsGraphicsSection);

  /* Get interface to delete */
  ifs = VEInternalContainerGet((VEINTERNALCONTAINER*)p_InterfacesContainer, interfaceID);
  if (!ifs)
  {
    VESectionLeaveInternal(p_SettingsGraphicsSection);
    return;
  }

  /* Disable current interface */
  if (p_SettingsGraphicsInterfaceID == interfaceID)
    p_SettingsGraphicsInterfaceID = 0;

  VEInterfaceDeleteInternal(ifs);
  VEInternalContainerRemove((VEINTERNALCONTAINER*)p_InterfacesContainer, interfaceID);
  VESectionLeaveInternal(p_SettingsGraphicsSection);
} /* End of 'VEInterfaceDelete' function */

/***
 * PURPOSE: Create new empty interface
 *  RETURN: Created interface ID if success, 0 otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEInterfaceCreate( VEVOID )
{
  VEINTERFACE *newInterface = New(sizeof(VEINTERFACE), "Interface");
  if (!newInterface)
    return 0;

  /* Critical section creation */
  newInterface->m_Windows = VEInternalContainerCreate(VE_DEFAULT_CONTAINERSIZE, "Windows container");
  if (!newInterface->m_Windows)
  {
    Delete(newInterface);
    return 0;
  }

  /* That's it */
  return VEInternalContainerAdd((VEINTERNALCONTAINER*)p_InterfacesContainer, newInterface);
} /* End of 'VEInterfaceCreate' function */


/***
 * PURPOSE: Load window from file to interface
 *  RETURN: Loaded window identifier if success, 0 otherwise
 *   PARAM: [IN] interfaceID - interface identifier
 *   PARAM: [IN] filename    - window file nam
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEInterfaceWindowLoad( const VEUINT interfaceID, const VEBUFFER filename )
{
  VEWINDOW *window = NULL;
  VEINTERFACE *ifs = VEInternalContainerGet((VEINTERNALCONTAINER*)p_InterfacesContainer, interfaceID);
  if (!ifs)
    return 0;

  /* Try to load window from file */
  window = VEWindowLoadInternal(filename);
  if (!window)
  {
    VEConsolePrintArg("Can't load window: ", filename);
    return 0;
  }


  /* Adding window to an interface */
  VEConsolePrintArg("Window loaded: ", filename);
  return VEInternalContainerAdd(ifs->m_Windows, window);
} /* End of 'VEGUIWindowLoad' function */

/***
 * PURPOSE: Unload existing window
 *   PARAM: [IN] interfaceID - interface identifier
 *   PARAM: [IN] windowID    - window identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfaceWindowUnload( const VEUINT interfaceID, const VEUINT windowID )
{
  VEWINDOW *window = NULL;
  VEINTERFACE *ifs = VEInternalContainerGet((VEINTERNALCONTAINER*)p_InterfacesContainer, interfaceID);
  if (!ifs)
    return;

  /* Try to load window from file */
  window = VEInternalContainerGet(ifs->m_Windows, windowID);
  if (!window)
    return;

  /* Remove window */
  VEInternalContainerRemove(ifs->m_Windows, windowID);
  VEWindowDeleteInternal(window);
} /* End of 'VEGUIWindowUnload' function */

/***
 * PURPOSE: Select interface to render
 *   PARAM: [IN] interfaceID - interface identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfaceSelect( const VEUINT interfaceID )
{
  VEINTERFACE *ifs = VEInternalContainerGet((VEINTERNALCONTAINER*)p_InterfacesContainer, interfaceID);
  if (!ifs)
    return;

  /* Setup current interface */
  p_SettingsGraphicsInterfaceID = interfaceID;
} /* End of 'VEInterfaceSelect' function */

/***
 * PURPOSE: Render current widget
 *   PARAM: [IN] widget         - pointer to widget to render
 *   PARAM: [IN] windowPosition - parent window position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWidgetRenderInternal( VEWIDGET *widget, VEVECTOR2D windowPosition )
{
  VEVECTOR2D position = VEVector2DAdd(windowPosition, widget->m_Dimensions.m_Position);

  /* Switch widget type */
  switch(widget->m_Type)
  {
    case VE_WIDGET_LABEL:
      VEWidgetLabelRenderInternal((VEWIDGETLABEL*)widget->m_Control, position);
      break;
    case VE_WIDGET_IMAGE:
      break;
    case VE_WIDGET_BUTTON:
      VEWidgetButtonRenderInternal((VEWIDGETBUTTON*)widget->m_Control, position, widget->m_Dimensions.m_Size);
      break;
    case VE_WIDGET_EDIT:
      break;
  }
} /* End of 'VEWidgetRenderInternal' function */

/***
 * PURPOSE: Render current window
 *   PARAM: [IN] window - pointer to window to render
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWindowRenderInternal( VEWINDOW *window )
{
  VEUINT widgetID = 0;
  VEVECTOR4D lightBorder = VEVector4DMult(window->m_Color, 1.5);
  VEVECTOR4D darkBorder  = VEVector4DMult(window->m_Color, 0.5);

  if (!window->m_IsVisible)
    return;

  /* Window background */
  glBegin(GL_QUADS);
    glColor4f(window->m_Color.m_X, window->m_Color.m_Y, window->m_Color.m_Z, window->m_Color.m_W);

    glVertex2i(window->m_Dimensions.m_Position.m_X,                                   window->m_Dimensions.m_Position.m_Y);
    glVertex2i(window->m_Dimensions.m_Position.m_X + window->m_Dimensions.m_Size.m_X, window->m_Dimensions.m_Position.m_Y);
    glVertex2i(window->m_Dimensions.m_Position.m_X + window->m_Dimensions.m_Size.m_X, window->m_Dimensions.m_Position.m_Y + window->m_Dimensions.m_Size.m_Y);
    glVertex2i(window->m_Dimensions.m_Position.m_X,                                   window->m_Dimensions.m_Position.m_Y + window->m_Dimensions.m_Size.m_Y);
  glEnd();

  /* Window border */
  glBegin(GL_LINES);

    /* Light border */
    glColor4f(lightBorder.m_X, lightBorder.m_Y, lightBorder.m_Z, window->m_Color.m_W);
    glVertex2i(window->m_Dimensions.m_Position.m_X,                                   window->m_Dimensions.m_Position.m_Y);
    glVertex2i(window->m_Dimensions.m_Position.m_X + window->m_Dimensions.m_Size.m_X, window->m_Dimensions.m_Position.m_Y);
    glVertex2i(window->m_Dimensions.m_Position.m_X,                                   window->m_Dimensions.m_Position.m_Y);
    glVertex2i(window->m_Dimensions.m_Position.m_X,                                   window->m_Dimensions.m_Position.m_Y + window->m_Dimensions.m_Size.m_Y);

    /* Dark border */
    glColor4f(darkBorder.m_X,  darkBorder.m_Y,  darkBorder.m_Z,  window->m_Color.m_W);
    glVertex2i(window->m_Dimensions.m_Position.m_X + window->m_Dimensions.m_Size.m_X, window->m_Dimensions.m_Position.m_Y);
    glVertex2i(window->m_Dimensions.m_Position.m_X + window->m_Dimensions.m_Size.m_X, window->m_Dimensions.m_Position.m_Y + window->m_Dimensions.m_Size.m_Y);
    glVertex2i(window->m_Dimensions.m_Position.m_X + window->m_Dimensions.m_Size.m_X, window->m_Dimensions.m_Position.m_Y + window->m_Dimensions.m_Size.m_Y);
    glVertex2i(window->m_Dimensions.m_Position.m_X,                                   window->m_Dimensions.m_Position.m_Y + window->m_Dimensions.m_Size.m_Y);
  glEnd();

  /* Output window header */
  if (window->m_Title)
  {
    /* Output header */
    VEGraphicsPrint(window->m_Dimensions.m_Position.m_X+3, window->m_Dimensions.m_Position.m_Y+15, VECOLOR_GREEN, window->m_Title->m_Data);

    /* Draw header line */
    glBegin(GL_LINES);
      glColor4f(lightBorder.m_X, lightBorder.m_Y, lightBorder.m_Z, window->m_Color.m_W);
      glVertex2i(window->m_Dimensions.m_Position.m_X + 1,                                   window->m_Dimensions.m_Position.m_Y + VE_WINDOW_HEADER_HEIGHT);
      glVertex2i(window->m_Dimensions.m_Position.m_X + window->m_Dimensions.m_Size.m_X - 1, window->m_Dimensions.m_Position.m_Y + VE_WINDOW_HEADER_HEIGHT);
    glEnd();
  }

  VESectionEnterInternal(window->m_Widgets->m_Sync);

  for (widgetID = 0; widgetID < window->m_Widgets->m_Size; widgetID++)
    if (window->m_Widgets->m_Items[widgetID])
    {
      VEWIDGET *widget = window->m_Widgets->m_Items[widgetID];
      if (widget->m_IsVisible)
        VEWidgetRenderInternal(widget, window->m_Dimensions.m_Position);
    }

  VESectionLeaveInternal(window->m_Widgets->m_Sync);
} /* End of 'VEWindowRenderInternal' function */

/***
 * PURPOSE: Render current interface
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfaceRenderInternal( VEVOID )
{
  VEINTERFACE *ifs = VEInternalContainerGet((VEINTERNALCONTAINER*)p_InterfacesContainer, p_SettingsGraphicsInterfaceID);

  /* Store values */
  VEBOOL blending = FALSE, lighting = FALSE;
  if (glIsEnabled(GL_BLEND))
    blending = TRUE;
  if (glIsEnabled(GL_LIGHTING))
    lighting = TRUE;

  /* Preparation */
  glDisable(GL_DEPTH_TEST);
  glDisable(GL_TEXTURE_2D);
  glDisable(GL_LIGHTING);
  glEnable(GL_BLEND);

  /* Render windows */
  VEInternalContainerForeach(ifs->m_Windows, (VEFUNCTION)VEWindowRenderInternal);

  /* Restore values */
  if (lighting)
    glEnable(GL_LIGHTING);
  if (!blending)
    glDisable(GL_BLEND);
  else
    glEnable(GL_BLEND);

  /* Enable depth test */
  glEnable(GL_DEPTH_TEST);
} /* End of 'VEInterfaceRenderInternal' function */

/***
 * PURPOSE: Mouse move processor
 *  RETURN: TRUE if event was processed, FALSE otherwise
 *   PARAM: [IN] event - mouse moving event
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEInterfaceMouseMoveProcessor( const VEMOUSE event )
{
  VEUINT windowID = 0;
  VEINTERFACE *ifs = VEInternalContainerGet((VEINTERNALCONTAINER*)p_InterfacesContainer, p_SettingsGraphicsInterfaceID);
  if (!ifs)
    return FALSE;

  /* Not left mouse button */
  if (!event.m_Left)
    return FALSE;

  VESectionEnterInternal(ifs->m_Windows->m_Sync);

  /* Moving window */
  for (windowID = 0; windowID < ifs->m_Windows->m_Size; windowID++)
    if (ifs->m_Windows->m_Items[windowID])
    {
      VEWINDOW *window = ifs->m_Windows->m_Items[windowID];
      if (!window->m_IsMoveable)
        continue;

      if ((event.m_X > window->m_Dimensions.m_Position.m_X)&&(event.m_X < window->m_Dimensions.m_Position.m_X + window->m_Dimensions.m_Size.m_X))
        if ((event.m_Y > window->m_Dimensions.m_Position.m_Y)&&(event.m_Y < window->m_Dimensions.m_Position.m_Y + VE_WINDOW_HEADER_HEIGHT))
        {
          window->m_Dimensions.m_Position.m_X = event.m_X - window->m_Dimensions.m_Size.m_X / 2;
          window->m_Dimensions.m_Position.m_Y = event.m_Y - VE_WINDOW_HEADER_HEIGHT * 0.5;

          /* Check screen borders (horizontal) */
          if (window->m_Dimensions.m_Position.m_X + window->m_Dimensions.m_Size.m_X > p_GraphicsScreenWidth)
            window->m_Dimensions.m_Position.m_X = p_GraphicsScreenWidth - window->m_Dimensions.m_Size.m_X;
          if (window->m_Dimensions.m_Position.m_X < 0)
            window->m_Dimensions.m_Position.m_X = 0;

          /* Check screen borders (vertical) */
          if (window->m_Dimensions.m_Position.m_Y + window->m_Dimensions.m_Size.m_Y > p_GraphicsScreenHeight)
            window->m_Dimensions.m_Position.m_Y = p_GraphicsScreenHeight - window->m_Dimensions.m_Size.m_Y;
          if (window->m_Dimensions.m_Position.m_Y < 0)
            window->m_Dimensions.m_Position.m_Y = 0;

          /* That's it */
          VESectionLeaveInternal(ifs->m_Windows->m_Sync);
          return TRUE;
        }
    }

  VESectionLeaveInternal(ifs->m_Windows->m_Sync);

  /* That's it */
  return FALSE;
} /* End of 'VEInterfaceMouseMoveProcessor' function */

/***
 * PURPOSE: Mouse click processor
 *  RETURN: TRUE if event was processed, FALSE otherwise
 *   PARAM: [IN] event - mouse click event
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEInterfaceMouseClickProcessor( const VEMOUSE event )
{
  return FALSE;
} /* End of 'VEInterfaceMouseClickProcessor' function */

/***
 * PURPOSE: Set new window position
 *   PARAM: [IN] interfaceID - interface identifier
 *   PARAM: [IN] windowID    - window identifier
 *   PARAM: [IN] position    - new window position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfaceWindowSetPosition( const VEUINT interfaceID, const VEUINT windowID, const VEVECTOR2D position )
{
  VEWINDOW *window = NULL;
  VEINTERFACE *ifs = VEInternalContainerGet((VEINTERNALCONTAINER*)p_InterfacesContainer, interfaceID);
  if (!ifs)
    return;

  /* Get window pointer */
  window = VEInternalContainerGet(ifs->m_Windows, windowID);
  if (!window)
    return;

  /* Set new position */
  window->m_Dimensions.m_Position = position;
} /* End of '' function */

/***
 * PURPOSE: Register widget processor
 *   PARAM: [IN] interfaceID - interface identifier
 *   PARAM: [IN] windowID    - window identifier
 *   PARAM: [IN] widgetID    - widget identifier
 *   PARAM: [IN] processor   - event processor
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfaceWidgetSetProcessor( const VEUINT interfaceID, const VEUINT windowID, const VEBUFFER widgetID, const VEEVENTPROCESSOR processor )
{
  VEUINT wID = 0;
  VEWINDOW *window = NULL;
  VEINTERFACE *ifs = VEInternalContainerGet((VEINTERNALCONTAINER*)p_InterfacesContainer, interfaceID);
  if (!ifs)
    return;

  /* Get window pointer */
  window = VEInternalContainerGet(ifs->m_Windows, windowID);
  if (!window)
    return;

  VESectionEnterInternal(window->m_Widgets->m_Sync);
  for (wID = 0; wID < window->m_Widgets->m_Size; wID++)
    if (window->m_Widgets->m_Items[wID])
    {
      VEWIDGET *currentWidget = (VEWIDGET*)window->m_Widgets->m_Items[wID];
      if (currentWidget->m_ID)
        if (VEBuffersEqual(currentWidget->m_ID->m_Data, widgetID))
        {
          currentWidget->m_Processor = processor;
          VESectionLeaveInternal(window->m_Widgets->m_Sync);
          return;
        }
    }

  VESectionLeaveInternal(window->m_Widgets->m_Sync);
} /* End of 'VEInterfaceWidgetSetProcessor' function */

/***
 * PURPOSE: Set widget property
 *   PARAM: [IN] interfaceID  - interface identifier
 *   PARAM: [IN] windowID     - window identifier
 *   PARAM: [IN] widgetID     - widget identifier
 *   PARAM: [IN] propertyName - property name
 *   PARAM: [IN] value        - property value
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfaceWidgetSetProperty( const VEUINT interfaceID, const VEUINT windowID, const VEBUFFER widgetID, const VEBUFFER propertyName, const VEBUFFER value )
{
  VEUINT wID = 0;
  VEWIDGET *widget = NULL;
  VEWINDOW *window = NULL;
  VEINTERFACE *ifs = VEInternalContainerGet((VEINTERNALCONTAINER*)p_InterfacesContainer, interfaceID);
  if (!ifs)
    return;

  /* Get window pointer */
  window = VEInternalContainerGet(ifs->m_Windows, windowID);
  if (!window)
    return;

  VESectionEnterInternal(window->m_Widgets->m_Sync);
  for (wID = 0; (wID < window->m_Widgets->m_Size)&&(!widget); wID++)
    if (window->m_Widgets->m_Items[wID])
    {
      VEWIDGET *currentWidget = (VEWIDGET*)window->m_Widgets->m_Items[wID];
      if (currentWidget->m_ID)
        if (VEBuffersEqual(currentWidget->m_ID->m_Data, widgetID))
          widget = currentWidget;
    }

  if (widget)
    switch(widget->m_Type)
    {
      case VE_WIDGET_LABEL:
        VEWidgetLabelInjectAttributeInternal((VEWIDGETLABEL*)widget->m_Control, propertyName, value);
        break;
      case VE_WIDGET_IMAGE:
        break;
      case VE_WIDGET_BUTTON:
        break;
      case VE_WIDGET_EDIT:
        break;
    }

  VESectionLeaveInternal(window->m_Widgets->m_Sync);
} /* End of 'VEInterfaceWidgetSetProperty' function */
