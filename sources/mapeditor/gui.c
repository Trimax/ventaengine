#include "editor.h"

/* Interface identifier */
VEUINT interfaceID = 0;

/* Mode window identifier */
VEUINT wndInformation   = 0;

/* Main window identifier */
VEUINT wndMain = 0;

/***
 * PURPOSE: Map editor
 *   PARAM: [IN] event - mouse event
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID EditorGUICreate( VEVOID )
{
  interfaceID    = VEInterfaceCreate();
  wndInformation = VEInterfaceWindowLoad(interfaceID, "data/gui/mapeditor/information.window");
  wndMain        = VEInterfaceWindowLoad(interfaceID, "data/gui/mapeditor/main.window");

  /* Select created interface */
  VEInterfaceSelect(interfaceID);
} /* End of 'EditorGUICreate' function */
