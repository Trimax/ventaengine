#ifndef EDITOR_H_INCLUDED
#define EDITOR_H_INCLUDED

#include "engine/engine.h"

/*** Global definitions ***/

/* Scene identifier */
extern VEUINT sceneID;

/* Camera identifier */
extern VEUINT cameraID;

/* Light identifier */
extern VEUINT lightID;

/* Interface identifier */
extern VEUINT interfaceID;
extern VEUINT wndInformation;

/*** Functions ***/

/***
 * PURPOSE: Map editor keyboard processor
 *   PARAM: [IN] key - pressed key
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID EditorKeyboardProcessor( VEKEY key );

/***
 * PURPOSE: Map editor mouse processor
 *   PARAM: [IN] event - mouse event
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID EditorMouseProcessor( VEMOUSE event );

/***
 * PURPOSE: Map editor
 *   PARAM: [IN] event - mouse event
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID EditorGUICreate( VEVOID );

#endif // EDITOR_H_INCLUDED
