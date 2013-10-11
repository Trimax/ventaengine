#include "internalglut.h"

#include "internalinterfacemanager.h"
#include "internalconsole.h"
#include "internalinput.h"
#include "types.h"

#include <string.h>

/* Maximal key code */
#define VE_INPUT_MAXKEY 280

/* Maximal mouse buttons number */
#define VE_INPUT_MAXBUTTONS 3

/* Mouse buttons */
#define VE_MOUSE_BUTTONLEFT   0
#define VE_MOUSE_BUTTONMIDDLE 1
#define VE_MOUSE_BUTTONRIGHT  2

/* Keyboard keys state */
VEBOOL p_InputKeys[VE_INPUT_MAXKEY];

/* Keyboard call-back */
VEKEYBOARDPROCESSOR p_InputProcessor = NULL;

/* Mouse buttons state */
VEBOOL p_MouseButtons[VE_INPUT_MAXBUTTONS];

/* Mouse call-back */
VEMOUSEPROCESSOR p_MouseProcessor = NULL;

/***
 * PURPOSE: Process mouse click
 *   PARAM: [IN] button - number of clicked button
 *   PARAM: [IN] state  - button state
 *   PARAM: [IN] x      - new mouse horizontal position
 *   PARAM: [IN] y      - new mouse vertical position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMouseProcessorInternal( VEINT button, VEINT state, VEINT x, VEINT y )
{
  VEMOUSE event;

  /* Change button state */
  if (state == GLUT_DOWN)
    p_MouseButtons[button] = TRUE;
  else
    p_MouseButtons[button] = FALSE;

  /* Create event */
  event.m_Left   = p_MouseButtons[GLUT_LEFT_BUTTON];
  event.m_Middle = p_MouseButtons[GLUT_MIDDLE_BUTTON];
  event.m_Right  = p_MouseButtons[GLUT_RIGHT_BUTTON];

  /* Store position */
  event.m_X = x;
  event.m_Y = y;

  /* Execute interface mouse processor */
  if (VEInterfaceMouseClickProcessor(event))
    return;

  /* Execute user's mouse processor */
  if (p_MouseProcessor)
    p_MouseProcessor(event);
} /* End of 'VEMouseProcessorInternal' function */

/***
 * PURPOSE: Process mouse moving
 *   PARAM: [IN] x - new mouse horizontal position
 *   PARAM: [IN] y - new mouse vertical position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMouseMoveInternal( VEINT x, VEINT y )
{
  VEMOUSE event;

  /* Create event */
  event.m_Left   = p_MouseButtons[GLUT_LEFT_BUTTON];
  event.m_Middle = p_MouseButtons[GLUT_MIDDLE_BUTTON];
  event.m_Right  = p_MouseButtons[GLUT_RIGHT_BUTTON];

  /* Store position */
  event.m_X = x;
  event.m_Y = y;

  /* Execute interface mouse processor */
  if (VEInterfaceMouseMoveProcessor(event))
    return;

  /* Execute user's mouse processor */
  if (p_MouseProcessor)
    p_MouseProcessor(event);
} /* End of 'VEMouseMoveInternal' function */

/***
 * PURPOSE: Initialize input manager
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] keyboardProcessor - callback function to process user input
 *   PARAM: [IN] mouseProcessor    - callback function to process mouse events
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEInputInit( VEKEYBOARDPROCESSOR keyboardProcessor, VEMOUSEPROCESSOR mouseProcessor )
{
  p_MouseProcessor = mouseProcessor;
  p_InputProcessor = keyboardProcessor;
  memset(p_InputKeys, 0, VE_INPUT_MAXKEY);
  memset(p_MouseButtons, 0, VE_INPUT_MAXBUTTONS);

  /* Register processors for simple keys (<= 255) */
  glutKeyboardFunc(VEInputKeyPressedInternal);
  glutKeyboardUpFunc(VEInputKeyReleasedInternal);

  /* Register processors for special keys */
  glutSpecialFunc(VEInputSpecialKeyPressedInternal);
  glutSpecialUpFunc(VEInputSpecialKeyReleasedInternal);

  /* Register mouse processor */
  glutMouseFunc(VEMouseProcessorInternal);
  glutMotionFunc(VEMouseMoveInternal);
  glutPassiveMotionFunc(VEMouseMoveInternal);

  /* That's it */
  return TRUE;
} /* End of 'VEInputInit' function */

/***
 * PURPOSE: Handle key pressing
 *   PARAM: [IN] key - pressed key code
 *   PARAM: [IN] x   - mouse cursor horizontal position
 *   PARAM: [IN] y   - mouse cursor vertical position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInputKeyPressedInternal( VEBYTE key, VEINT x, VEINT y )
{
  VEINT modifiers = glutGetModifiers();
  VEKEY event;

  event.m_Code  = key;
  event.m_Alt   = modifiers&GLUT_ACTIVE_ALT;
  event.m_Ctrl  = modifiers&GLUT_ACTIVE_CTRL;
  event.m_Shift = modifiers&GLUT_ACTIVE_SHIFT;
  event.m_State = TRUE;

  /* Release key */
  p_InputKeys[key] = TRUE;

  /* Keyboard processor registered */
  if (p_InputProcessor)
    p_InputProcessor(event);
} /* End of 'VEInputKeyPressedInternal' function */

/***
 * PURPOSE: Handle key releasing
 *   PARAM: [IN] key - pressed key code
 *   PARAM: [IN] x   - mouse cursor horizontal position
 *   PARAM: [IN] y   - mouse cursor vertical position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInputKeyReleasedInternal( VEBYTE key, VEINT x, VEINT y )
{
  VEINT modifiers = glutGetModifiers();
  VEKEY event;

  event.m_Code  = key;
  event.m_Alt   = modifiers&GLUT_ACTIVE_ALT;
  event.m_Ctrl  = modifiers&GLUT_ACTIVE_CTRL;
  event.m_Shift = modifiers&GLUT_ACTIVE_SHIFT;
  event.m_State = FALSE;

  p_InputKeys[key] = FALSE;

  /* Try to process key using console */
  if (VEConsoleKeyPressed(event))
    return;

  /* Keyboard processor registered */
  if (p_InputProcessor)
    p_InputProcessor(event);
} /* End of 'VEInputKeyReleasedInternal' function */

/***
 * PURPOSE: Map GLUT keys to Venta Engine keys
 *  RETURN: Venta Engine key code
 *   PARAM: [IN] key - pressed key code (GLUT code)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEInputMapKeyInternal( VEINT key )
{
  /* Functional keys */
  if ((key >= 1)&&(key <= 12))
    return 255 + key;

  if ((key >= 100)&&(key <= 108))
    return 168 + key;

  /* Unknown code */
  return 0;
} /* End of 'VEInputMapKeyInternal' function */

/***
 * PURPOSE: Handle special key pressing
 *   PARAM: [IN] key - pressed key code
 *   PARAM: [IN] x   - mouse cursor horizontal position
 *   PARAM: [IN] y   - mouse cursor vertical position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInputSpecialKeyPressedInternal( VEINT key, VEINT x, VEINT y )
{
  VEUINT mappedKey = VEInputMapKeyInternal(key);
  if (mappedKey < VE_INPUT_MAXKEY)
    p_InputKeys[mappedKey] = TRUE;
} /* End of 'VEInputSpecialKeyPressedInternal' function */

/***
 * PURPOSE: Handle special key pressing
 *   PARAM: [IN] key - pressed key code
 *   PARAM: [IN] x   - mouse cursor horizontal position
 *   PARAM: [IN] y   - mouse cursor vertical position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInputSpecialKeyReleasedInternal( VEINT key, VEINT x, VEINT y )
{
  VEUINT mappedKey = VEInputMapKeyInternal(key);
  if ((mappedKey < VE_INPUT_MAXKEY)&&(mappedKey > 0))
  {
    VEINT modifiers = glutGetModifiers();
    VEKEY event;

    event.m_Code  = mappedKey;
    event.m_Alt   = modifiers&GLUT_ACTIVE_ALT;
    event.m_Ctrl  = modifiers&GLUT_ACTIVE_CTRL;
    event.m_Shift = modifiers&GLUT_ACTIVE_SHIFT;

    p_InputKeys[mappedKey] = FALSE;

    /* Try to process key using console */
    if (VEConsoleKeyPressed(event))
      return;

    /* Keyboard processor registered */
    if (p_InputProcessor)
      p_InputProcessor(event);
  }
} /* End of 'VEInputSpecialKeyReleasedInternal' function */

/***
 * PURPOSE: Deinitialize input manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInputDeinit( VEVOID )
{
  p_InputProcessor = NULL;
  p_MouseProcessor = NULL;
} /* End of 'VEInputDeinit' function */

/***
 * PURPOSE: Check key state
 *  RETURN: TRUE if key is pressed, FALSE otherwise
 *   PARAM: [IN] keyCode - code of key (see VE_KEY_XXX definitions)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsKeyPressed( const VEUINT keyCode )
{
  if (keyCode >= VE_INPUT_MAXKEY)
    return FALSE;
  return p_InputKeys[keyCode];
} /* End of 'VEIsKeyPressed' function */
