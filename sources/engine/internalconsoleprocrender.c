#include "internalglut.h"

#include "internalsettings.h"
#include "internalconsole.h"
#include "internalbuffer.h"
#include "internalstring.h"
#include "console.h"

/*** Render command arguments ***/

/* Enable/disable axes rendering */
#define VE_CONSOLE_COMMANDRENDER_AXES    "axes"

/* Enable/disable forces rendering */
#define VE_CONSOLE_COMMANDRENDER_FORCES  "forces"

/* Enable/disable lights rendering */
#define VE_CONSOLE_COMMANDRENDER_LIGHTS  "lights"

/* Enable/disable cameras rendering */
#define VE_CONSOLE_COMMANDRENDER_CAMERAS "cameras"

/* Enable/disable rendering type (wire/solid/point) */
#define VE_CONSOLE_COMMANDRENDER_SOLID  "solid"
#define VE_CONSOLE_COMMANDRENDER_WIRE   "wire"
#define VE_CONSOLE_COMMANDRENDER_POINTS "points"

/* Enable/disable debug rendering mode */
#define VE_CONSOLE_COMMANDRENDER_DEBUG "debug"

/* Enable/disable shaders */
#define VE_CONSOLE_COMMANDRENDER_SHADERS "shaders"

/***
 * PURPOSE: Process render command
 *  RETURN: TRUE command processed, FALSE otherwise
 *   PARAM: [IN] cmdParams - command arguments
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEConsoleProcessorRender( const VEARRAY *cmdParams )
{
  VESTRING *parameter = NULL;
  if (cmdParams->m_Size <= 1)
  {
    VEConsolePrint("  not enough parameters. Type '" VE_CONSOLE_RENDER " " VE_CONSOLE_COMMANDHELP "' to see help");
    return TRUE;
  }

  parameter = (VESTRING*)cmdParams->m_Items[1];
  if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDRENDER_FORCES))
  {
    p_SettingsGraphicsRenderForces = !p_SettingsGraphicsRenderForces;
    if (p_SettingsGraphicsRenderForces)
      VEConsolePrint("  forces rendering enabled");
    else
      VEConsolePrint("  forces rendering enabled");

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDRENDER_AXES))
  {
    p_SettingsGraphicsRenderAxes = !p_SettingsGraphicsRenderAxes;
    if (p_SettingsGraphicsRenderAxes)
      VEConsolePrint("  axes rendering enabled");
    else
      VEConsolePrint("  axes rendering enabled");

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDRENDER_DEBUG))
  {
    p_SettingsGraphicsRenderSlow = !p_SettingsGraphicsRenderSlow;
    if (p_SettingsGraphicsRenderSlow)
      VEConsolePrint("  slow rendering enabled");
    else
      VEConsolePrint("  fast rendering enabled");

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDRENDER_SHADERS))
  {
    p_SettingsGraphicsShaders = !p_SettingsGraphicsShaders;
    if (p_SettingsGraphicsShaders)
      VEConsolePrint("  shaders enabled");
    else
      VEConsolePrint("  shaders disabled");

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDRENDER_LIGHTS))
  {
    p_SettingsGraphicsRenderLights = !p_SettingsGraphicsRenderLights;
    if (p_SettingsGraphicsRenderLights)
      VEConsolePrint("  light spheres rendering enabled");
    else
      VEConsolePrint("  light spheres rendering disabled");

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDRENDER_CAMERAS))
  {
    p_SettingsGraphicsRenderCameras = !p_SettingsGraphicsRenderCameras;
    if (p_SettingsGraphicsRenderCameras)
      VEConsolePrint("  cameras rendering enabled");
    else
      VEConsolePrint("  cameras rendering disabled");

    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDRENDER_SOLID))
  {
    p_SettingsGraphicsRenderType = GL_TRIANGLES;
    VEConsolePrint("  rendering type: SOLID");
    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDRENDER_WIRE))
  {
    p_SettingsGraphicsRenderType = GL_LINES;
    VEConsolePrint("  rendering type: WIRED");
    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDRENDER_POINTS))
  {
    p_SettingsGraphicsRenderType = GL_POINTS;
    VEConsolePrint("  rendering type: POINTS");
    return TRUE;
  } else if (VEBuffersEqual(parameter->m_Data, VE_CONSOLE_COMMANDHELP))
  {
    VEConsolePrint("Command 'render'");
    VEConsolePrint("  " VE_CONSOLE_RENDER " " VE_CONSOLE_COMMANDHELP           " - show help screen");
    VEConsolePrint("  " VE_CONSOLE_RENDER " " VE_CONSOLE_COMMANDRENDER_AXES    " - show world system coordinates");
    VEConsolePrint("  " VE_CONSOLE_RENDER " " VE_CONSOLE_COMMANDRENDER_FORCES  " - show forces, applied to objects");
    VEConsolePrint("  " VE_CONSOLE_RENDER " " VE_CONSOLE_COMMANDRENDER_LIGHTS  " - show light sources");
    VEConsolePrint("  " VE_CONSOLE_RENDER " " VE_CONSOLE_COMMANDRENDER_CAMERAS " - show cameras");
    VEConsolePrint("  " VE_CONSOLE_RENDER " " VE_CONSOLE_COMMANDRENDER_SOLID   " - render all objects solid");
    VEConsolePrint("  " VE_CONSOLE_RENDER " " VE_CONSOLE_COMMANDRENDER_WIRE    " - render all objects wired");
    VEConsolePrint("  " VE_CONSOLE_RENDER " " VE_CONSOLE_COMMANDRENDER_POINTS  " - render all objects as points");
    VEConsolePrint("  " VE_CONSOLE_RENDER " " VE_CONSOLE_COMMANDRENDER_DEBUG   " - render all scene slowly, with TBN");
    VEConsolePrint("  " VE_CONSOLE_RENDER " " VE_CONSOLE_COMMANDRENDER_SHADERS " - render all scene with shaders");
    return TRUE;
  }

  /* That's it */
  return FALSE;
} /* End of 'VEConsoleProcessorRender' function */
