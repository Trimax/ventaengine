#ifndef GRAPHICS_H_INCLUDED
#define GRAPHICS_H_INCLUDED

#include "types.h"

/* Default screen width */
#define VE_DEFAULT_SCREENWIDTH 800

/* Default screen height */
#define VE_DEFAULT_SCREENHEIGHT 600

/* Far plane distance */
#define VE_DEFAULT_FARPLANE 1000.0

/*** Screen parameters ***/
extern VEUSHORT p_GraphicsScreenWidth;
extern VEUSHORT p_GraphicsScreenHeight;

/***
 * PURPOSE: Initialize graphics engine
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] screenWidth  - render window screen width
 *   PARAM: [IN] screenHeight - render window screen height
 *   PARAM: [IN] fullscreen   - full screen mode flag
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEGraphicsInit( const VEUSHORT screenWidth, const VEUSHORT screenHeight, const VEBOOL fullscreen );

/***
 * PURPOSE: Determine graphics engine state
 *  RETURN: TRUE if engine initialized, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEGraphicsIsInitialized( VEVOID );

/***
 * PURPOSE: Deinitialize graphics engine
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGraphicsDeinit( VEVOID );

#endif // GRAPHICS_H_INCLUDED
