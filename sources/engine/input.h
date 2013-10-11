#ifndef INPUT_H_INCLUDED
#define INPUT_H_INCLUDED

#include "types.h"

/* Pressed key event structure */
typedef struct tagVEKEY
{
  VEINT  m_Code;  /* Pressed key code */
  VEBOOL m_Ctrl;  /* CTRL state */
  VEBOOL m_Alt;   /* ALT state */
  VEBOOL m_Shift; /* SHIFT state */
  VEBOOL m_State; /* TRUE if key was pressed, FALSE if released */
} VEKEY;

/* Mouse event structure */
typedef struct tagVEMOUSE
{
  VEUINT m_X;      /* Cursor X position */
  VEUINT m_Y;      /* Cursor Y position */

  VEBOOL m_Left;   /* Left button pressed flag */
  VEBOOL m_Middle; /* Middle button pressed flag */
  VEBOOL m_Right;  /* Right button pressed flag */
} VEMOUSE;

/*** Function keys definition ***/
#define VE_KEY_F1         256
#define VE_KEY_F2         257
#define VE_KEY_F3         258
#define VE_KEY_F4         259
#define VE_KEY_F5         260
#define VE_KEY_F6         261
#define VE_KEY_F7         262
#define VE_KEY_F8         263
#define VE_KEY_F9         264
#define VE_KEY_F10        265
#define VE_KEY_F11        266
#define VE_KEY_F12        267

/*** Directional keys ***/
#define VE_KEY_LEFT       268
#define VE_KEY_UP         269
#define VE_KEY_RIGHT      270
#define VE_KEY_DOWN       271
#define VE_KEY_PAGEUP     272
#define VE_KEY_PAGEDOWN   273
#define VE_KEY_HOME       274
#define VE_KEY_END        275
#define VE_KEY_INSERT     276

/*** Manage keys ***/
#define VE_KEY_ESCAPE      27
#define VE_KEY_ENTER       13
#define VE_KEY_BACKSPACE   8
#define VE_KEY_TAB         9

/*** Some other codes ***/
#define VE_KEY_TILDE       126
#define VE_KEY_EQUAL       61
#define VE_KEY_MULT        42
#define VE_KEY_PLUS        43
#define VE_KEY_MINUS       45
#define VE_KEY_SPACE       32
#define VE_KEY_SLASH       47
#define VE_KEY_BACKSLASH   92
#define VE_KEY_VERTICAL    124
#define VE_KEY_UNDERLINE   95

/*** Point, comma, question, warning, double point, point+comma ***/
#define VE_KEY_POINT       46
#define VE_KEY_COMMA       44
#define VE_KEY_QUESTION    63
#define VE_KEY_EXCLAMATION 33
#define VE_KEY_COLON       58
#define VE_KEY_SEMICOLON   59

/*** Brackets ***/
#define VE_KEY_BRACKETANGLEOPEN   60
#define VE_KEY_BRACKETANGLECLOSE  62
#define VE_KEY_BRACKETROUNDOPEN   40
#define VE_KEY_BRACKETROUNDCLOSE  41
#define VE_KEY_BRACKETRECTOPEN    91
#define VE_KEY_BRACKETRECTCLOSE   93
#define VE_KEY_BRACKETFIGUREOPEN  123
#define VE_KEY_BRACKETFIGURECLOSE 125

/*** Quotes ***/
#define VE_KEY_APOSTROPHE  96
#define VE_KEY_QUOTESINGLE 39
#define VE_KEY_QUOTEDOUBLE 34

/*** Other ***/
#define VE_KEY_DOG         64
#define VE_KEY_LATTICE     35
#define VE_KEY_DOLLAR      36
#define VE_KEY_PERCENT     37
#define VE_KEY_HOUSE       94
#define VE_KEY_AMPERSAND   38

/* Keyboard processor function */
typedef VEVOID (*VEFUNCKEYBOARDPROCESSOR)(VEKEY);
typedef VEFUNCKEYBOARDPROCESSOR VEKEYBOARDPROCESSOR;

/* Mouse processor function */
typedef VEVOID (*VEFUNCMOUSEPROCESSOR)(VEMOUSE);
typedef VEFUNCMOUSEPROCESSOR VEMOUSEPROCESSOR;

/***
 * PURPOSE: Check key state
 *  RETURN: TRUE if key is pressed, FALSE otherwise
 *   PARAM: [IN] keyCode - code of key (see VE_KEY_XXX definitions)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsKeyPressed( const VEUINT keyCode );

#endif // INPUT_H_INCLUDED
