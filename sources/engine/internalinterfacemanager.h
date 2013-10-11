#ifndef INTERNALINTERFACEMANAGER_H_INCLUDED
#define INTERNALINTERFACEMANAGER_H_INCLUDED

#include "input.h"
#include "types.h"

/***
 * PURPOSE: Initialize interfaces manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEInterfacesInit( VEVOID );

/***
 * PURPOSE: Render current interface
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfaceRenderInternal( VEVOID );

/***
 * PURPOSE: Mouse move processor
 *  RETURN: TRUE if event was processed, FALSE otherwise
 *   PARAM: [IN] event - mouse moving event
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEInterfaceMouseMoveProcessor( const VEMOUSE event );

/***
 * PURPOSE: Mouse click processor
 *  RETURN: TRUE if event was processed, FALSE otherwise
 *   PARAM: [IN] event - mouse click event
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEInterfaceMouseClickProcessor( const VEMOUSE event );

/***
 * PURPOSE: Deinitialize interfaces manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInterfacesDeinit( VEVOID );

#endif // INTERNALINTERFACEMANAGER_H_INCLUDED
