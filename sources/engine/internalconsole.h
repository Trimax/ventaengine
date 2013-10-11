#ifndef INTERNALCONSOLE_H_INCLUDED
#define INTERNALCONSOLE_H_INCLUDED

#include "internalarray.h"
#include "types.h"
#include "input.h"

/*** Console commands ***/

/* Quit & exit command */
#define VE_CONSOLE_COMMANDQUIT "quit"
#define VE_CONSOLE_COMMANDEXIT "exit"

/* Version command */
#define VE_CONSOLE_COMMANDVER     "ver"
#define VE_CONSOLE_COMMANDVERSION "version"

/* Help command */
#define VE_CONSOLE_COMMANDHELP "help"

/** Graphics **/
#define VE_CONSOLE_RENDER "render"

/*** Resources ***/
#define VE_CONSOLE_RESOURCE "resource"

/*** Scenes ***/
#define VE_CONSOLE_SCENE "scene"

/*** Objects ***/
#define VE_CONSOLE_OBJECT "object"

/*** Lights ***/
#define VE_CONSOLE_LIGHT "light"

/*** Cameras ***/
#define VE_CONSOLE_CAMERA "camera"

/***
 * PURPOSE: Initialize console
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] consoleProcesssor - user-defined console processor
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleInit( VEFUNCTIONCONSOLEPROCESSOR consoleProcesssor );

/***
 * PURPOSE: Render console
 *   PARAM: [IN] FPS - number of frames per second
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsoleRenderInternal( const VEUINT FPS );

/***
 * PURPOSE: Executes user-defined console processor
 *  RETURN: TRUE if command processed, FALSE otherwise
 *   PARAM: [IN] command - command to process
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleExecuteProcessorInternal( const VEBUFFER command );

/***
 * PURPOSE: Console key processor
 *  RETURN: TRUE if key was processed, FALSE otherwise
 *   PARAM: [IN] key - accepted key
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleKeyPressed( const VEKEY key );

/***
 * PURPOSE: Deinitialize console
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConsoleDeinit( VEVOID );

/*** Commands processors ***/

/***
 * PURPOSE: Process render command
 *  RETURN: TRUE command processed, FALSE otherwise
 *   PARAM: [IN] cmdParams - command arguments
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleProcessorRender( const VEARRAY *cmdParams );

/***
 * PURPOSE: Process render command
 *  RETURN: TRUE command processed, FALSE otherwise
 *   PARAM: [IN] cmdParams - command arguments
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleProcessorScene( const VEARRAY *cmdParams );

/***
 * PURPOSE: Process resource command
 *  RETURN: TRUE command processed, FALSE otherwise
 *   PARAM: [IN] cmdParams - command arguments
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleProcessorResource( const VEARRAY *cmdParams );

/***
 * PURPOSE: Process object command
 *  RETURN: TRUE command processed, FALSE otherwise
 *   PARAM: [IN] cmdParams - command arguments
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleProcessorObject( const VEARRAY *cmdParams );

/***
 * PURPOSE: Process camera command
 *  RETURN: TRUE command processed, FALSE otherwise
 *   PARAM: [IN] cmdParams - command arguments
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleProcessorCamera( const VEARRAY *cmdParams );

/***
 * PURPOSE: Process camera command
 *  RETURN: TRUE command processed, FALSE otherwise
 *   PARAM: [IN] cmdParams - command arguments
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleProcessorLight( const VEARRAY *cmdParams );

#endif // INTERNALCONSOLE_H_INCLUDED
