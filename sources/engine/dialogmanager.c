#include "internaldialogmanager.h"
#include "internalbuffer.h"
#include "memorymanager.h"
#include "console.h"
#include "logger.h"

#include <string.h>
#include <stdio.h>

/***
 * PURPOSE: Deletes widget
 *   PARAM: [IN] widget - pointer to widget to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWidgetDeleteInternal( VEWIDGET *widget )
{
  switch(widget->m_Type)
  {
    case VE_WIDGET_LABEL:
      VEWidgetLabelDeleteInternal((VEWIDGETLABEL*)widget->m_Control);
      break;
    case VE_WIDGET_IMAGE:
      break;
    case VE_WIDGET_BUTTON:
      VEWidgetButtonDeleteInternal((VEWIDGETBUTTON*)widget->m_Control);
      break;
    case VE_WIDGET_EDIT:
      break;
  }

  /* Delete identifier */
  VEStringDeleteInternal(widget->m_ID);

  /* Self deletion */
  Delete(widget);
} /* End of 'VEWidgetDeleteInternal' function */

/***
 * PURPOSE: Load widget from file
 *  RETURN: Pointer to loaded widget if success, NULL otherwise
 *   PARAM: [IN]     filename    - file name
 *   PARAM: [IN]     widgetType  - one of VE_WIDGET_XXX definitions
 *   PARAM: [IN/OUT] currentLine - pointer to line counter
 *  AUTHOR: Eliseev Dmitry
 ***/
VEWIDGET *VEWidgetLoad( FILE *f, const VEBYTE widgetType, VEUINT *currentLine )
{
  VEUINT separatorPosition = 0, length = 0;
  VEWIDGET *widget = New(sizeof(VEWIDGET), "Widget");
  if (!widget)
    return NULL;

  /* Store widget type */
  widget->m_Type = widgetType;
  widget->m_IsVisible = TRUE;

  switch(widgetType)
  {
    case VE_WIDGET_LABEL:
      widget->m_Control = New(sizeof(VEWIDGETLABEL), "Label widget");
      break;
    case VE_WIDGET_IMAGE:
      break;
    case VE_WIDGET_BUTTON:
      widget->m_Control = New(sizeof(VEWIDGETBUTTON), "Button widget");
      break;
    case VE_WIDGET_EDIT:
      break;
  }

  if (!widget->m_Control)
  {
    Delete(widget);
    return NULL;
  }

  /* Reading widget properties */
  while (!feof(f))
  {
    VECHAR line[VE_BUFFER_LARGE*2+1], name[VE_BUFFER_LARGE], value[VE_BUFFER_LARGE];
    memset(line,  0, VE_BUFFER_LARGE*2+1);
    memset(name,  0, VE_BUFFER_LARGE);
    memset(value, 0, VE_BUFFER_LARGE);

    /* Reading line */
    fgets(line, VE_BUFFER_LARGE, f);
    VEBufferTrim(line, ' ');
    length = strlen(line);

    if (length <= 1)
    {
      (*currentLine) += 1;
      continue;
    }

    if (VEBuffersEqual(line, "\r\n")||VEBuffersEqual(line, "\n"))
    {
      currentLine++;
      continue;
    }

    /* Skip comment line */
    if (line[0] == VE_SYMBOL_COMMENT)
    {
      (*currentLine) += 1;
      continue;
    }

    /* End of child */
    if (line[0] == VE_SYMBOL_CHILDEND)
    {
      (*currentLine) += 1;
      return widget;
    }

    /* Search for separator */
    for (separatorPosition = 0; (separatorPosition < length)&&(line[separatorPosition] != '='); separatorPosition++);
    if (separatorPosition == length)
    {
      VECHAR message[VE_BUFFER_SMALL];
      memset(message, 0, VE_BUFFER_SMALL);
      sprintf(message, "Error at line: %d: Separator symbol wasn't found", *currentLine);

      /* Output message to console */
      VELoggerError(message);
      VEConsolePrint(message);

      /* Parsing error */
      VEWidgetDeleteInternal(widget);
      return NULL;
    }

    /* Extract name and value */
    memcpy(name,  &line[0], separatorPosition);
    memcpy(value, &line[separatorPosition+1], length - separatorPosition + 1);

    /* Inject attributes to widget */
    if (VEBuffersEqual(name, VE_PARAM_ID))
      widget->m_ID = VEStringCreateFromBufferInternal(value);
    else if (VEBuffersEqual(name, VE_PARAM_WIDTH))
      widget->m_Dimensions.m_Size.m_X = VEBufferToReal(value);
    else if (VEBuffersEqual(name, VE_PARAM_HEIGHT))
      widget->m_Dimensions.m_Size.m_Y = VEBufferToReal(value);
    else if (VEBuffersEqual(name, VE_PARAM_X))
      widget->m_Dimensions.m_Position.m_X = VEBufferToReal(value);
    else if (VEBuffersEqual(name, VE_PARAM_Y))
      widget->m_Dimensions.m_Position.m_Y = VEBufferToReal(value);
    else
      switch(widgetType)
      {
        case VE_WIDGET_LABEL:
          VEWidgetLabelInjectAttributeInternal((VEWIDGETLABEL*)widget->m_Control, name, value);
          break;
        case VE_WIDGET_IMAGE:
          break;
        case VE_WIDGET_BUTTON:
          VEWidgetButtonInjectAttributeInternal((VEWIDGETBUTTON*)widget->m_Control, name, value);
          break;
        case VE_WIDGET_EDIT:
          break;
      }

    (*currentLine) += 1;
  }

  return widget;
} /* End of 'VEWidgetLoad' function */

/***
 * PURPOSE: Load window from file
 *  RETURN: Pointer to loaded window if success, NULL otherwise
 *   PARAM: [IN] filename - file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEWINDOW *VEWindowLoadInternal( const VEBUFFER filename )
{
  VEBOOL    success = TRUE;
  VEUINT    currentLine = 1, separatorPosition = 0, length = 0;
  FILE     *f = NULL;
  VEWINDOW *window = NULL;

  /* Wrong argument*/
  if (!filename)
    return NULL;

  /* Try to open file to read */
  f = fopen(filename, "rt");
  if (!f)
    return NULL;

  /* Memory allocation */
  window = New(sizeof(VEWINDOW), "Window creation");
  if (!window)
  {
    fclose(f);
    return NULL;
  }

  /* Default flags */
  window->m_IsMoveable = TRUE;
  window->m_IsVisible  = TRUE;

  window->m_Widgets = VEInternalContainerCreate(VE_DEFAULT_CONTAINERSIZE, "Widgets container");
  if (!window->m_Widgets)
  {
    fclose(f);
    Delete(window);
    return NULL;
  }

  /* Try to read */
  while ((!feof(f))&&(success))
  {
    VECHAR line[VE_BUFFER_LARGE*2+1], name[VE_BUFFER_LARGE], value[VE_BUFFER_LARGE];
    memset(line,  0, VE_BUFFER_LARGE*2+1);
    memset(name,  0, VE_BUFFER_LARGE);
    memset(value, 0, VE_BUFFER_LARGE);

    /* Reading line */
    fgets(line, VE_BUFFER_LARGE, f);
    VEBufferTrim(line, ' ');
    length = strlen(line);

    if (length <= 1)
    {
      currentLine++;
      continue;
    }

    if (VEBuffersEqual(line, "\r\n")||VEBuffersEqual(line, "\n"))
    {
      currentLine++;
      continue;
    }


    /* Skip comment line */
    if (line[0] == VE_SYMBOL_COMMENT)
    {
      currentLine++;
      continue;
    }

    /* Child object started */
    if (line[0] == VE_SYMBOL_CHILDBEGIN)
    {
      VEBYTE widgetType = 0;
      VEUINT pos = 0;
      VEWIDGET *widget = NULL;
      currentLine++;

      /* Unknown child type */
      if (length <= 2)
      {
        success = FALSE;
        continue;
      }

      /* Trim opening brace */
      for (pos = 1; (pos < length)&&(line[pos] != '\n')&&(line[pos] != '\r'); pos++)
        line[pos-1] = line[pos];
      memset(&line[pos-1], 0, VE_BUFFER_LARGE*2+1 - pos);

      /* Determine widget type */
      if (VEBuffersEqual(line, VE_WIDGETSTR_LABEL))
        widgetType = VE_WIDGET_LABEL;
      else if (VEBuffersEqual(line, VE_WIDGETSTR_IMAGE))
        widgetType = VE_WIDGET_IMAGE;
      else if (VEBuffersEqual(line, VE_WIDGETSTR_BUTTON))
        widgetType = VE_WIDGET_BUTTON;
      else if (VEBuffersEqual(line, VE_WIDGETSTR_EDIT))
        widgetType = VE_WIDGET_EDIT;
      else
      {
        success = FALSE;
        continue;
      }

      /* Loading widget */
      widget = VEWidgetLoad(f, widgetType, &currentLine);
      if (widget)
        VEInternalContainerAdd(window->m_Widgets, widget);
      else
        success = FALSE;
      continue;
    }

    /* Search for separator */
    for (separatorPosition = 0; (separatorPosition < length)&&(line[separatorPosition] != '='); separatorPosition++);
    if (separatorPosition == length)
    {
      VECHAR message[VE_BUFFER_SMALL];
      memset(message, 0, VE_BUFFER_SMALL);
      sprintf(message, "Error at line: %d: Separator symbol wasn't found", currentLine);

      /* Output message to console */
      VELoggerError(message);
      VEConsolePrint(message);

      /* Parsing error */
      success = FALSE;
      continue;
    }

    /* Extract name and value */
    memcpy(name,  &line[0], separatorPosition);
    memcpy(value, &line[separatorPosition+1], length - separatorPosition + 1);

    /* Fill fields */
    if (VEBuffersEqual(name, VE_PARAM_ID))
      window->m_ID = VEStringCreateFromBufferInternal(value);
    else if (VEBuffersEqual(name, VE_PARAM_TITLE))
      window->m_Title = VEStringCreateFromBufferInternal(value);
    else if (VEBuffersEqual(name, VE_PARAM_WIDTH))
      window->m_Dimensions.m_Size.m_X = VEBufferToReal(value);
    else if (VEBuffersEqual(name, VE_PARAM_HEIGHT))
      window->m_Dimensions.m_Size.m_Y = VEBufferToReal(value);
    else if (VEBuffersEqual(name, VE_PARAM_X))
      window->m_Dimensions.m_Position.m_X = VEBufferToReal(value);
    else if (VEBuffersEqual(name, VE_PARAM_Y))
      window->m_Dimensions.m_Position.m_Y = VEBufferToReal(value);
    else if (VEBuffersEqual(name, VE_PARAM_COLOR))
      window->m_Color = VEColorToVector4D(VEBufferToColor(value));
    else
    {
      VECHAR message[VE_BUFFER_SMALL];
      memset(message, 0, VE_BUFFER_SMALL);
      sprintf(message, "Error at line: %d: Unknown attribute: %s", currentLine, name);

      /* Output message to console */
      VELoggerError(message);
      VEConsolePrint(message);
    }

    /* Next line */
    currentLine++;
  }

  /* Window wasn't loaded */
  if (!success)
  {
    fclose(f);
    VEWindowDeleteInternal(window);
    return NULL;
  }

  /* That's it */
  fclose(f);
  return window;
} /* End of 'VEWindowLoad' function */

/***
 * PURPOSE: Deletes window
 *   PARAM: [IN] window - pointer to window to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWindowDeleteInternal( VEWINDOW *window )
{
  if (!window)
    return;

  /* Remove identifier & title */
  VEStringDeleteInternal(window->m_ID);
  VEStringDeleteInternal(window->m_Title);

  /* Remove widgets */
  VEInternalContainerForeach(window->m_Widgets, (VEFUNCTION)VEWidgetDeleteInternal);
  VEInternalContainerDelete(window->m_Widgets);

  Delete(window);
} /* End of 'VEWindowDeleteInternal' function */
