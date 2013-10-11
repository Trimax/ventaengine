#include "internalglut.h"

#include "internalgraphics.h"
#include "internalbuffer.h"
#include "internaltext.h"

/* Available fonts */
VEPOINTER glutFonts[7] = {GLUT_BITMAP_9_BY_15, GLUT_BITMAP_8_BY_13, GLUT_BITMAP_TIMES_ROMAN_10, GLUT_BITMAP_TIMES_ROMAN_24,
                          GLUT_BITMAP_HELVETICA_10, GLUT_BITMAP_HELVETICA_12, GLUT_BITMAP_HELVETICA_18};

/***
 * PURPOSE: Print string to screen
 *   PARAM: [IN] x       - horizontal string position
 *   PARAM: [IN] y       - vertical string position
 *   PARAM: [IN] color   - text color
 *   PARAM: [IN] message - message to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGraphicsPrint( const VEUINT x, const VEUINT y, const VECOLOR color, const VEBUFFER message )
{
  VEGraphicsPrintUsingVector(x, y, VEColorToVector4D(color), message);
} /* End of 'VEGraphicsPrint' function */

/***
 * PURPOSE: Print string to screen
 *   PARAM: [IN] x       - horizontal string position
 *   PARAM: [IN] y       - vertical string position
 *   PARAM: [IN] color   - text color
 *   PARAM: [IN] message - message to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEGraphicsPrintUsingVector( const VEUINT x, const VEUINT y, const VEVECTOR4D color, const VEBUFFER message )
{
  VEBOOL blending = FALSE;
  VEBUFFER text = message;

  if (!message)
    return;

  /* Empty message */
  if (VEBufferLength(message) == 0)
    return;

  /* Store blending state */
  if (glIsEnabled(GL_BLEND))
    blending = TRUE;

  /* Enable blending */
  glEnable(GL_BLEND);

  /* Setting message color */
  glColor4f(color.m_X, color.m_Y, color.m_Z, color.m_W);

  /* Move pointer to startup position */
  glRasterPos2f(x, y);

  /* Drawing message */
  while (*text) {
    glutBitmapCharacter(glutFonts[1], *text);
    text++;
  }

  /* Restore blending state */
  if(!blending)
    glDisable(GL_BLEND);
} /* End of 'VEGraphicsPrint' function */
