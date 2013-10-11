#ifndef TEXT_H_INCLUDED
#define TEXT_H_INCLUDED

#include "types.h"
#include "color.h"

/*** Font parameters ***/

/* Symbol width */
#define VE_FONT_SYMBOL_WIDTH  7.8

/* Symbol height */
#define VE_FONT_SYMBOL_HEIGHT 13.0

/***
 * PURPOSE: Print string to screen
 *   PARAM: [IN] x       - horizontal string position
 *   PARAM: [IN] y       - vertical string position
 *   PARAM: [IN] color   - text color
 *   PARAM: [IN] message - message to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGraphicsPrint( const VEUINT x, const VEUINT y, const VECOLOR color, const VEBUFFER message );

/***
 * PURPOSE: Print string to screen
 *   PARAM: [IN] x       - horizontal string position
 *   PARAM: [IN] y       - vertical string position
 *   PARAM: [IN] color   - text color
 *   PARAM: [IN] message - message to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGraphicsPrintUsingVector( const VEUINT x, const VEUINT y, const VEVECTOR4D color, const VEBUFFER message );

#endif // TEXT_H_INCLUDED
