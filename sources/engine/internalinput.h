#ifndef INTERNALINPUT_H_INCLUDED
#define INTERNALINPUT_H_INCLUDED

#include "input.h"

/***
 * PURPOSE: Initialize input manager
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] keyboardProcessor - callback function to process user input
 *   PARAM: [IN] mouseProcessor    - callback function to process mouse events
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEInputInit( VEKEYBOARDPROCESSOR keyboardProcessor, VEMOUSEPROCESSOR mouseProcessor );

/***
 * PURPOSE: Handle key pressing
 *   PARAM: [IN] key - pressed key code
 *   PARAM: [IN] x   - mouse cursor horizontal position
 *   PARAM: [IN] y   - mouse cursor vertical position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInputKeyPressedInternal( VEBYTE key, VEINT x, VEINT y );

/***
 * PURPOSE: Handle key releasing
 *   PARAM: [IN] key - pressed key code
 *   PARAM: [IN] x   - mouse cursor horizontal position
 *   PARAM: [IN] y   - mouse cursor vertical position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInputKeyReleasedInternal( VEBYTE key, VEINT x, VEINT y );

/***
 * PURPOSE: Handle special key pressing
 *   PARAM: [IN] key - pressed key code
 *   PARAM: [IN] x   - mouse cursor horizontal position
 *   PARAM: [IN] y   - mouse cursor vertical position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInputSpecialKeyPressedInternal( VEINT key, VEINT x, VEINT y );

/***
 * PURPOSE: Handle special key pressing
 *   PARAM: [IN] key - pressed key code
 *   PARAM: [IN] x   - mouse cursor horizontal position
 *   PARAM: [IN] y   - mouse cursor vertical position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInputSpecialKeyReleasedInternal( VEINT key, VEINT x, VEINT y );

/***
 * PURPOSE: Deinitialize input manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInputDeinit( VEVOID );

#endif // INTERNALINPUT_H_INCLUDED
