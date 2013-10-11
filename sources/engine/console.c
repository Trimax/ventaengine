#include "internalglut.h"

#include "internalcriticalsection.h"
#include "structuresmanager.h"
#include "internalconsole.h"
#include "internalstring.h"
#include "internallogger.h"
#include "internalbuffer.h"
#include "internalarray.h"
#include "memorymanager.h"
#include "internaltext.h"
#include "console.h"
#include "color.h"
#include "time.h"
#include "math.h"

#include <string.h>
#include <stdio.h>

/* History size */
#define VE_CONSOLE_HISTORYSIZE 2048

/* Line height */
#define VE_CONSOLE_LINEHEIGHT 10

/* Console structure */
typedef struct tagVECONSOLE
{
  VECHAR                     m_Line[VE_BUFFER_EXTRALARGE]; /* Current command buffer */
  VEUINT                     m_Caret;                      /* Caret position at current buffer */
  VEINT                      m_CommandPosition;            /* Pointer position at commands history */
  VEARRAY                   *m_History;                    /* Full history */
  VEARRAY                   *m_CommandsHistory;            /* Commands history */
  VEUINT                     m_StartFrom;                  /* History displayng (start from line) */
  VECRITICALSECTION         *m_Section;                    /* Critical section */
  VEBOOL                     m_IsHidden;                   /* Is console hidden */
  VEFUNCTIONCONSOLEPROCESSOR m_Processor;                  /* User-defined console processor */
  VEULONG                    m_CaretPeriod;                /* Caret visibility period */
} VECONSOLE;

/* Console structure */
volatile static VECONSOLE *p_Console = NULL;

/***
 * PURPOSE: Initialize console
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] consoleProcesssor - user-defined console processor
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleInit( VEFUNCTIONCONSOLEPROCESSOR consoleProcesssor )
{
  p_Console = New(sizeof(VECONSOLE), "Console");
  if (!p_Console)
    return FALSE;

  /* Critical section creation */
  p_Console->m_Section = VESectionCreateInternal("Console section");
  if (!p_Console)
  {
    VEConsoleDeinit();
    return FALSE;
  }

  /* History creation */
  p_Console->m_History = VEArrayCreateInternal(VE_CONSOLE_HISTORYSIZE);
  if (!p_Console->m_History)
  {
    VEConsoleDeinit();
    return FALSE;
  }

  /* History creation */
  p_Console->m_CommandsHistory = VEArrayCreateInternal(VE_CONSOLE_HISTORYSIZE);
  if (!p_Console->m_CommandsHistory)
  {
    VEConsoleDeinit();
    return FALSE;
  }

  /* Console is hidden by default */
  p_Console->m_IsHidden = TRUE;

  /* There were no commands in history */
  p_Console->m_CommandPosition = -1;
  p_Console->m_CaretPeriod = VETime();

  /* That's it */
  return TRUE;
} /* End of 'VEConsoleInit' function */

/***
 * PURPOSE: Print a message to console internal
 *   PARAM: [IN] message - message to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrintInternal( const VEBUFFER message )
{
  VEUINT len = VEBufferLength(message);
  VEINT pos = 0;

  /* Remove last item */
  VEStringDeleteInternal(p_Console->m_History->m_Items[VE_CONSOLE_HISTORYSIZE-1]);

  /* Moving all items up */
  for (pos = p_Console->m_History->m_Size-2; pos >= 0; pos--)
    p_Console->m_History->m_Items[pos+1] = p_Console->m_History->m_Items[pos];

  /* Create new string */
  p_Console->m_History->m_Items[0] = VEStringCreateInternal(len+1);
  if (!p_Console->m_History->m_Items[0])
    return;

  ((VESTRING*)p_Console->m_History->m_Items[0])->m_Length = len;
  ((VESTRING*)p_Console->m_History->m_Items[0])->m_Size = len+1;
  memcpy(((VESTRING*)p_Console->m_History->m_Items[0])->m_Data, message, len);
} /* End of 'VEConsolePrintInternal' function */

/***
 * PURPOSE: Print a message to console
 *   PARAM: [IN] message - message to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrint( const VEBUFFER message )
{
  if (!(message&&p_Console))
    return;

  VESectionEnterInternal(p_Console->m_Section);
  VEConsolePrintInternal(message);
  VESectionLeaveInternal(p_Console->m_Section);
} /* End of 'VEConsolePrint' function */

/***
 * PURPOSE: Print a message with argument to console
 *   PARAM: [IN] message - message to print
 *   PARAM: [IN] arg     - message's argument
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrintArg( const VEBUFFER message, const VEBUFFER arg )
{
  VEUINT lenMsg = VEBufferLength(message), lenArg = VEBufferLength(arg);
  VESTRING *fullMessage = VEStringCreateInternal(lenMsg + lenArg + 1);
  if (!fullMessage)
    return;

  /* Wrong arguments */
  if (!(message && arg))
  {
    VEStringDeleteInternal(fullMessage);
    return;
  }

  /* Copy data */
  memcpy(fullMessage->m_Data, message, lenMsg);
  memcpy(&fullMessage->m_Data[lenMsg], arg, lenArg);

  /* Output message */
  VEConsolePrint(fullMessage->m_Data);

  /* Delete temporary string */
  VEStringDeleteInternal(fullMessage);
} /* End of 'VEConsolePrintArg' function */

/***
 * PURPOSE: Deinitialize console
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsoleDeinit( VEVOID )
{
  if (!p_Console)
    return;

  /* Remove all commands history */
  VEArrayForeachInternal(p_Console->m_CommandsHistory, (VEFUNCTION)VEStringDeleteInternal);
  VEArrayDeleteInternal(p_Console->m_CommandsHistory);

  /* Remove all history */
  VEArrayForeachInternal(p_Console->m_History, (VEFUNCTION)VEStringDeleteInternal);
  VEArrayDeleteInternal(p_Console->m_History);

  /* Remove critical section */
  VESectionDeleteInternal(p_Console->m_Section);

  /* Release memory */
  Delete((VEPOINTER)p_Console);
  p_Console = NULL;
} /* End of 'VEConsoleDeinit' function */

/***
 * PURPOSE: Render console
 *   PARAM: [IN] width  - console width
 *   PARAM: [IN] height - console height
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsoleRenderLinesInternal( const VEUINT width, const VEUINT height )
{
  VEULONG now = VETime();
  VEINT pageSize = height / VE_CONSOLE_LINEHEIGHT - 2, pos = 0;
  VEINT lastItem = VEMIN(VE_CONSOLE_HISTORYSIZE, p_Console->m_StartFrom + pageSize);

  /* Too small window */
  if (pageSize <= 0)
    return;

  VESectionEnterInternal(p_Console->m_Section);

  /* Print history */
  for (pos = p_Console->m_StartFrom; (pos < lastItem)&&(p_Console->m_History->m_Items[pos]); pos++)
  {
    VEINT position = height - 10 * (pos - p_Console->m_StartFrom) - 20;
    VEBUFFER message = ((VESTRING*)p_Console->m_History->m_Items[pos])->m_Data;
    VEGraphicsPrint(0, position, VECOLOR_WHITE, message);
  }

  /* Print current command */
  VEGraphicsPrint(0, height - VE_CONSOLE_LINEHEIGHT / 2, VECOLOR_GREEN, (VEBUFFER)p_Console->m_Line);

  /* Render caret just a half a second */
  if ((now - p_Console->m_CaretPeriod) > 500)
  {
    VEUINT offset = VEBufferLength((VEBUFFER)p_Console->m_Line) * 8;
    VEGraphicsPrint(offset, height - VE_CONSOLE_LINEHEIGHT / 2, VECOLOR_YELLOW, "_");
  }

  /* Reset caret rendering time */
  if ((now - p_Console->m_CaretPeriod) > 1000.0)
    p_Console->m_CaretPeriod = VETime();

  VESectionLeaveInternal(p_Console->m_Section);
} /* End of 'VEConsoleRenderLinesInternal' function */

/***
 * PURPOSE: Render console
 *   PARAM: [IN] FPS - number of frames per second
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsoleRenderInternal( const VEUINT FPS )
{
  static VECHAR bufferFPS[VE_BUFFER_SMALL];

  if (!p_Console)
    return;

  if (p_Console->m_IsHidden)
    return;

  /* Determine window size */
  VEINT consoleWidth  = glutGet(GLUT_WINDOW_WIDTH);
  VEINT consoleHeight = glutGet(GLUT_WINDOW_HEIGHT) / 2;

  VEBOOL blending = FALSE, lighting = FALSE;
  if (glIsEnabled(GL_BLEND))
    blending = TRUE;
  if (glIsEnabled(GL_LIGHTING))
    lighting = TRUE;

  /* Display console rectangle (half a window) */
  glDisable(GL_TEXTURE_2D);
  glDisable(GL_LIGHTING);
  glDisable(GL_DEPTH_TEST);
  glEnable(GL_BLEND);
  glBegin(GL_QUADS);
    glColor4f(0.5, 0.5, 0.5, 0.7);

    glVertex2i(0, 0);
    glVertex2i(consoleWidth, 0);
    glVertex2i(consoleWidth, consoleHeight);
    glVertex2i(0, consoleHeight);
  glEnd();

  if (!blending)
    glDisable(GL_BLEND);

  /* Output FPS */
  memset(bufferFPS, 0, VE_BUFFER_SMALL);
  sprintf(bufferFPS, "FPS: %d", FPS);
  VEGraphicsPrint(consoleWidth-80, 10, VECOLOR_RED, bufferFPS);

  /* Output console lines */
  VEConsoleRenderLinesInternal(consoleWidth, consoleHeight);
  glEnable(GL_DEPTH_TEST);

  if (lighting)
    glEnable(GL_LIGHTING);
} /* End of 'VEConsoleRenderInternal' function */

/***
 * PURPOSE: Check character by it's code for possibility to be printed
 *  RETURN: TRUE if character can be printed, FALSE otherwise
 *   PARAM: [IN] key - character code
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleIsPrintable( const VEINT key )
{
  /* Digits */
  if ((key >= 48)&&(key <= 57))
    return TRUE;

  /* Space */
  if (key == 32)
    return TRUE;

  /* Upper alphabet symbol */
  if ((key >= 65)&&(key <= 90))
    return TRUE;

  /* Lower alphabet symbol */
  if ((key >= 97)&&(key <= 122))
    return TRUE;

  /* Signs */
  if ((key == VE_KEY_PLUS)||(key == VE_KEY_MINUS)||(key == VE_KEY_EQUAL)||(key == VE_KEY_MULT))
    return TRUE;

  /* Slashes */
  if ((key == VE_KEY_SLASH)||(key == VE_KEY_BACKSLASH)||(key == VE_KEY_VERTICAL)||(key == VE_KEY_UNDERLINE))
    return TRUE;

  /* Comma, point, !, ?, :, ; */
  if ((key == VE_KEY_POINT)||(key == VE_KEY_COMMA)||(key == VE_KEY_QUESTION)||(key == VE_KEY_EXCLAMATION)||(key == VE_KEY_COLON)||(key == VE_KEY_SEMICOLON))
    return TRUE;

  /* Brackets <> */
  if ((key == VE_KEY_BRACKETANGLEOPEN)||(key == VE_KEY_BRACKETANGLECLOSE))
    return TRUE;

  /* Brackets () */
  if ((key == VE_KEY_BRACKETROUNDOPEN)||(key == VE_KEY_BRACKETROUNDCLOSE))
    return TRUE;

  /* Brackets [] */
  if ((key == VE_KEY_BRACKETRECTOPEN)||(key == VE_KEY_BRACKETRECTCLOSE))
    return TRUE;

  /* Brackets {} */
  if ((key == VE_KEY_BRACKETFIGUREOPEN)||(key == VE_KEY_BRACKETFIGURECLOSE))
    return TRUE;

  /* Quotes */
  if ((key == VE_KEY_QUOTESINGLE)||(key == VE_KEY_QUOTEDOUBLE))
    return TRUE;

  /* Other symbol */
  if ((key == VE_KEY_DOG)||(key == VE_KEY_LATTICE)||(key == VE_KEY_DOLLAR)||(key == VE_KEY_PERCENT)||(key == VE_KEY_HOUSE)||(key == VE_KEY_AMPERSAND))
    return TRUE;

  /* That's it */
  return FALSE;
} /* End of 'VEConsoleIsPrintable' function */

/***
 * PURPOSE: Store current command at commands history
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsoleCommandStore( VEVOID )
{
  VEUINT len = VEBufferLength((VECHAR*)p_Console->m_Line);
  VEINT pos = 0;

  /* Remove last item */
  VEStringDeleteInternal(p_Console->m_CommandsHistory->m_Items[VE_CONSOLE_HISTORYSIZE-1]);

  /* Moving all items up */
  for (pos = p_Console->m_CommandsHistory->m_Size-2; pos >= 0; pos--)
    p_Console->m_CommandsHistory->m_Items[pos+1] = p_Console->m_CommandsHistory->m_Items[pos];

  /* Create new string */
  p_Console->m_CommandsHistory->m_Items[0] = VEStringCreateInternal(len+1);
  if (!p_Console->m_CommandsHistory->m_Items[0])
    return;

  ((VESTRING*)p_Console->m_CommandsHistory->m_Items[0])->m_Length = len;
  ((VESTRING*)p_Console->m_CommandsHistory->m_Items[0])->m_Size = len+1;
  memcpy(((VESTRING*)p_Console->m_CommandsHistory->m_Items[0])->m_Data, (const VEPOINTER)p_Console->m_Line, len);

  /* Move to the first command position */
  p_Console->m_CommandPosition = -1;
} /* End of 'VEConsoleCommandStore' function */

/***
 * PURPOSE: Console key processor
 *  RETURN: TRUE if key was processed, FALSE otherwise
 *   PARAM: [IN] key - accepted key
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleKeyPressed( const VEKEY key )
{
  /* Show/hide console */
  if ((key.m_Code == VE_KEY_TILDE)||(key.m_Code == VE_KEY_APOSTROPHE))
  {
    p_Console->m_IsHidden = !p_Console->m_IsHidden;
    return TRUE;
  }

  /* Console is hidden */
  if (p_Console->m_IsHidden)
    return FALSE;

  /* Move history down */
  if (key.m_Code == VE_KEY_PAGEDOWN)
  {
    VEINT pageSize = glutGet(GLUT_WINDOW_HEIGHT) / (2 * VE_CONSOLE_LINEHEIGHT) - 2;
    p_Console->m_StartFrom = VEMAX(0, (VEINT)p_Console->m_StartFrom-pageSize);
    return TRUE;
  }

  /* Move history up */
  if (key.m_Code == VE_KEY_PAGEUP)
  {
    VEINT pageSize = glutGet(GLUT_WINDOW_HEIGHT) / (2 * VE_CONSOLE_LINEHEIGHT) - 2;
    VEINT upperCommand = VEMIN(VE_CONSOLE_HISTORYSIZE, (VEINT)p_Console->m_StartFrom+pageSize);
    if (!p_Console->m_History->m_Items[upperCommand])
      return TRUE;

    p_Console->m_StartFrom = VEMIN(VE_CONSOLE_HISTORYSIZE, (VEINT)p_Console->m_StartFrom+pageSize);
    return TRUE;
  }

  /* Strafe by commands history up */
  if (key.m_Code == VE_KEY_UP)
  {
    VESTRING *cmd = NULL;

    /* It's a top of history */
    if (p_Console->m_CommandPosition == VE_CONSOLE_HISTORYSIZE)
      return TRUE;

    /* Try to get previous command */
    cmd = p_Console->m_CommandsHistory->m_Items[p_Console->m_CommandPosition+1];
    if (cmd)
    {
      memset((VEPOINTER)p_Console->m_Line, 0, VE_BUFFER_EXTRALARGE);
      memcpy((VEPOINTER)p_Console->m_Line, cmd->m_Data, cmd->m_Length);
      p_Console->m_Caret = cmd->m_Length;
      p_Console->m_CommandPosition++;
    }

    /* Command processed */
    return TRUE;
  }

  /* Strafe by commands history down */
  if (key.m_Code == VE_KEY_DOWN)
  {
    VESTRING *cmd = NULL;

    /* It's a bottom of history */
    if (p_Console->m_CommandPosition <= 0)
    {
      memset((VEPOINTER)p_Console->m_Line, 0, VE_BUFFER_EXTRALARGE);
      p_Console->m_CommandPosition = -1;
      return TRUE;
    }

    /* Try to get previous command */
    cmd = p_Console->m_CommandsHistory->m_Items[--p_Console->m_CommandPosition];
    if (cmd)
    {
      memset((VEPOINTER)p_Console->m_Line, 0, VE_BUFFER_EXTRALARGE);
      memcpy((VEPOINTER)p_Console->m_Line, cmd->m_Data, cmd->m_Length);
      p_Console->m_Caret = cmd->m_Length;
    }

    /* Command processed */
    return TRUE;
  }

  /* Send command */
  if (key.m_Code == VE_KEY_ENTER)
  {
    if (p_Console->m_Caret > 0)
    {
      VEConsolePrint((VEBUFFER)p_Console->m_Line);
      if (!VEConsoleProcess((VEBUFFER)p_Console->m_Line))
        VEConsolePrint("  command failed");

      /* Store new command at history */
      VEConsoleCommandStore();
    }

    p_Console->m_Caret = 0;
    memset((VEPOINTER)p_Console->m_Line, 0, VE_BUFFER_EXTRALARGE);

    /* Enter processed */
    return TRUE;
  }

  /* Erase last symbol */
  if (key.m_Code == VE_KEY_BACKSPACE)
  {
    if (p_Console->m_Caret > 0)
    {
      p_Console->m_Caret--;
      p_Console->m_Line[p_Console->m_Caret] = 0;
    }

    /* Backspace processed */
    return TRUE;
  }

  /* If symbol is printable */
  if (VEConsoleIsPrintable(key.m_Code))
  {
    if (p_Console->m_Caret < VE_BUFFER_EXTRALARGE)
    {
      p_Console->m_Line[p_Console->m_Caret] = (VECHAR)key.m_Code;
      p_Console->m_Caret++;
    }

    /* Symbol printed */
    return TRUE;
  }

  /* Key processed */
  return FALSE;
} /* End of 'VEConsoleKeyPressed' function */

/***
 * PURPOSE: Executes user-defined console processor
 *  RETURN: TRUE if command processed, FALSE otherwise
 *   PARAM: [IN] command - command to process
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleExecuteProcessorInternal( const VEBUFFER command )
{
  if (!p_Console)
    return FALSE;

  if (p_Console->m_Processor)
    return p_Console->m_Processor(command);

  /* Command wasn't processed */
  return FALSE;
} /* End of 'VEConsoleExecuteProcessorInternal' function */

/***
 * PURPOSE: Print an integer to a console
 *   PARAM: [IN] message - message to print
 *   PARAM: [IN] number  - integer number to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrintUINT( const VEBUFFER message, const VEUINT number )
{
  VECHAR argument[VE_BUFFER_STANDART];
  memset(argument, 0, VE_BUFFER_STANDART);

  sprintf(argument, "%d", number);
  VEConsolePrintArg(message, argument);
} /* End of 'VEConsolePrintUINT' function */

/***
 * PURPOSE: Print a real to a console
 *   PARAM: [IN] message - message to print
 *   PARAM: [IN] number  - real number to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrintREAL( const VEBUFFER message, const VEREAL number )
{
  VECHAR argument[VE_BUFFER_STANDART];
  memset(argument, 0, VE_BUFFER_STANDART);

  sprintf(argument, "%.4lf", number);
  VEConsolePrintArg(message, argument);
} /* End of 'VEConsolePrintREAL' function */

/***
 * PURPOSE: Print a 2D vector to a console
 *   PARAM: [IN] message - message to print
 *   PARAM: [IN] vec     - 2D vector to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrintVECTOR2D( const VEBUFFER message, const VEVECTOR2D vec )
{
  VECHAR argument[VE_BUFFER_STANDART];
  memset(argument, 0, VE_BUFFER_STANDART);

  sprintf(argument, "[%.3lf,%.3lf]", vec.m_X, vec.m_Y);
  VEConsolePrintArg(message, argument);
} /* End of 'VEConsolePrintVECTOR2D' function */

/***
 * PURPOSE: Print a 3D vector to a console
 *   PARAM: [IN] message - message to print
 *   PARAM: [IN] vec     - 3D vector to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrintVECTOR3D( const VEBUFFER message, const VEVECTOR3D vec )
{
  VECHAR argument[VE_BUFFER_STANDART];
  memset(argument, 0, VE_BUFFER_STANDART);

  sprintf(argument, "[%.3lf,%.3lf,%.3lf]", vec.m_X, vec.m_Y, vec.m_Z);
  VEConsolePrintArg(message, argument);
} /* End of 'VEConsolePrintVECTOR3D' function */

/***
 * PURPOSE: Print a 4D vector to a console
 *   PARAM: [IN] message - message to print
 *   PARAM: [IN] vec     - 4D vector to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrintVECTOR4D( const VEBUFFER message, const VEVECTOR4D vec )
{
  VECHAR argument[VE_BUFFER_STANDART];
  memset(argument, 0, VE_BUFFER_STANDART);

  sprintf(argument, "[%.3lf,%.3lf,%.3lf,%.3lf]", vec.m_X, vec.m_Y, vec.m_Z, vec.m_W);
  VEConsolePrintArg(message, argument);
} /* End of 'VEConsolePrintVECTOR4D' function */

/***
 * PURPOSE: Print a color to a console
 *   PARAM: [IN] message - message to print
 *   PARAM: [IN] color   - color to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsolePrintCOLOR( const VEBUFFER message, const VECOLOR color )
{
  VECHAR argument[VE_BUFFER_STANDART];
  memset(argument, 0, VE_BUFFER_STANDART);

  sprintf(argument, "[%d,%d,%d,%d]", color.m_Red, color.m_Green, color.m_Blue, color.m_Alpha);
  VEConsolePrintArg(message, argument);
} /* End of 'VEConsolePrintCOLOR' function */
