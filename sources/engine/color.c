#include "color.h"

/*** Basic colors definition ***/
VECOLOR VECOLOR_WHITE = {255, 255, 255, 255};
VECOLOR VECOLOR_BLACK = {0,   0,   0,   255};
VECOLOR VECOLOR_RED   = {255, 0,   0,   255};
VECOLOR VECOLOR_GREEN = {0,   255, 0,   255};
VECOLOR VECOLOR_BLUE  = {0,   0,   255, 255};

/*** Extended colors definition ***/
VECOLOR VECOLOR_YELLOW  = {255, 255, 0,   255};
VECOLOR VECOLOR_GRAY    = {127, 127, 127, 255};
VECOLOR VECOLOR_BROWN   = {139, 69,  19,  255};
VECOLOR VECOLOR_ORANGE  = {255, 165, 0,   255};
VECOLOR VECOLOR_PINK    = {255, 20,  147, 255};

/***
 * PURPOSE: Create a new color by components
 *  RETURN: Created color
 *   PARAM: [IN] red   - red component
 *   PARAM: [IN] green - green component
 *   PARAM: [IN] blue  - blue component
 *   PARAM: [IN] alpha - alpha component (transparency)
 *  AUTHOR: Eliseev Dmitry
 ***/
VECOLOR VEColor( const VEBYTE red, const VEBYTE green, const VEBYTE blue, const VEBYTE alpha )
{
  VECOLOR color = {red, green, blue, alpha};
  return color;
} /* End of 'VEColor' function */

/***
 * PURPOSE: Transform VECOLOR to VEVECTOR4D
 *  RETURN: VEVECTOR4D representation of color
 *   PARAM: [IN] color - VECOLOR representation of color =)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR4D VEColorToVector4D( const VECOLOR color )
{
  VEVECTOR4D vec = {(VEREAL)color.m_Red / 255.0, (VEREAL)color.m_Green / 255.0, (VEREAL)color.m_Blue / 255.0, (VEREAL)color.m_Alpha / 255.0};
  return vec;
} /* End of 'VEColorToVector4d' function */
