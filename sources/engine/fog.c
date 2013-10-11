#include "internalscenemanager.h"
#include "internalsettings.h"
#include "console.h"
#include "fog.h"

/***
 * PURPOSE: Set fog density
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] sceneID - scene identifier to render
 *   PARAM: [IN] density - new fog density
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEFogSetDensity( const VEUINT sceneID, const VEREAL density )
{
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return FALSE;
  }

  if (!scene->m_Environment)
  {
    VEConsolePrint("Environment doesn't exist");
    return FALSE;
  }

  /* Store fog density */
  scene->m_Environment->m_Fog.m_Density = density;
  return TRUE;
} /* End of 'VEFogSetDensity' function */

/***
 * PURPOSE: Set fog color
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] sceneID - scene identifier to render
 *   PARAM: [IN] color   - new fog color
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEFogSetColor( const VEUINT sceneID, const VECOLOR color )
{
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return FALSE;
  }

  if (!scene->m_Environment)
  {
    VEConsolePrint("Environment doesn't exist");
    return FALSE;
  }

  /* Store fog density */
  scene->m_Environment->m_Fog.m_Color = VEColorToVector4D(color);
  return TRUE;
} /* End of 'VEFogSetColor' function */
