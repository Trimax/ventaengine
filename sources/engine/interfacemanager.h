#ifndef INTERFACEMANAGER_H_INCLUDED
#define INTERFACEMANAGER_H_INCLUDED

#include "types.h"
#include "math.h"

/*** Event types ***/

/* Mouse click */
#define VE_EVENT_CLICK      1

/* Key pressed */
#define VE_EVENT_KEYPRESSED 2

/* Interface event */
typedef struct tagVEEVENT
{
  VEBYTE m_Type;
} VEEVENT;

/* Widget event processor */
typedef VEVOID (*VEFUNCEVENTPROCESSOR)(VEEVENT);
typedef VEFUNCEVENTPROCESSOR VEEVENTPROCESSOR;

/***
 * PURPOSE: Create new empty interface
 *  RETURN: Created interface ID if success, 0 otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEInterfaceCreate( VEVOID );

/***
 * PURPOSE: Select interface to render
 *   PARAM: [IN] interfaceID - interface identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfaceSelect( const VEUINT interfaceID );

/***
 * PURPOSE: Load window from file to interface
 *  RETURN: Loaded window identifier if success, 0 otherwise
 *   PARAM: [IN] interfaceID - interface identifier
 *   PARAM: [IN] filename    - window file nam
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEInterfaceWindowLoad( const VEUINT interfaceID, const VEBUFFER filename );

/***
 * PURPOSE: Set new window position
 *   PARAM: [IN] interfaceID - interface identifier
 *   PARAM: [IN] windowID    - window identifier
 *   PARAM: [IN] position    - new window position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfaceWindowSetPosition( const VEUINT interfaceID, const VEUINT windowID, const VEVECTOR2D position );

/***
 * PURPOSE: Register widget processor
 *   PARAM: [IN] interfaceID - interface identifier
 *   PARAM: [IN] windowID    - window identifier
 *   PARAM: [IN] widgetID    - widget identifier
 *   PARAM: [IN] processor   - event processor
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfaceWidgetSetProcessor( const VEUINT interfaceID, const VEUINT windowID, const VEBUFFER widgetID, const VEEVENTPROCESSOR processor );

/***
 * PURPOSE: Set widget property
 *   PARAM: [IN] interfaceID  - interface identifier
 *   PARAM: [IN] windowID     - window identifier
 *   PARAM: [IN] widgetID     - widget identifier
 *   PARAM: [IN] propertyName - property name
 *   PARAM: [IN] value        - property value
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfaceWidgetSetProperty( const VEUINT interfaceID, const VEUINT windowID, const VEBUFFER widgetID, const VEBUFFER propertyName, const VEBUFFER value );

/***
 * PURPOSE: Unload existing window
 *   PARAM: [IN] interfaceID - interface identifier
 *   PARAM: [IN] windowID    - window identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfaceWindowUnload( const VEUINT interfaceID, const VEUINT windowID );

/***
 * PURPOSE: Delete existing interface
 *   PARAM: [IN] interfaceID - interface identifier to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfaceDelete( const VEUINT interfaceID );

#endif // INTERFACEMANAGER_H_INCLUDED
