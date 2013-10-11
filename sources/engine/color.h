#ifndef COLOR_H_INCLUDED
#define COLOR_H_INCLUDED

#include "vector4d.h"

/* Color structure */
typedef struct tagVECOLOR
{
  VEBYTE m_Red;   /* Red component */
  VEBYTE m_Green; /* Green component */
  VEBYTE m_Blue;  /* Blue component */
  VEBYTE m_Alpha; /* Alpha parameter (0 - absolute transparent) */
} VECOLOR;

/*** Basic colors ***/

/* White */
extern VECOLOR VECOLOR_WHITE;

/* Black */
extern VECOLOR VECOLOR_BLACK;

/* Red */
extern VECOLOR VECOLOR_RED;

/* Green */
extern VECOLOR VECOLOR_GREEN;

/* Blue */
extern VECOLOR VECOLOR_BLUE;


/*** Extended colors ***/

/* Yellow */
extern VECOLOR VECOLOR_YELLOW;

/* Gray */
extern VECOLOR VECOLOR_GRAY;

/* Brown */
extern VECOLOR VECOLOR_BROWN;

/* Orange */
extern VECOLOR VECOLOR_ORANGE;

/* Pink */
extern VECOLOR VECOLOR_PINK;

/***
 * PURPOSE: Create a new color by components
 *  RETURN: Created color
 *   PARAM: [IN] red   - red component
 *   PARAM: [IN] green - green component
 *   PARAM: [IN] blue  - blue component
 *   PARAM: [IN] alpha - alpha component (transparency)
 *  AUTHOR: Eliseev Dmitry
 ***/
VECOLOR VEColor( const VEBYTE red, const VEBYTE green, const VEBYTE blue, const VEBYTE alpha );

/***
 * PURPOSE: Transform VECOLOR to VEVECTOR4D
 *  RETURN: VEVECTOR4D representation of color
 *   PARAM: [IN] color - VECOLOR representation of color =)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR4D VEColorToVector4D( const VECOLOR color );

#endif // COLOR_H_INCLUDED
