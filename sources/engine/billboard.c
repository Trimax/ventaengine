#include "internalshadermanager.h"
#include "internalscenemanager.h"
#include "internalbillboard.h"
#include "internalsettings.h"
#include "memorymanager.h"
#include "billboard.h"

/***
 * PURPOSE: Creates new billboard
 *  RETURN: Created billboard identifier if success, 0 otherwise
 *   PARAM: [IN] sceneID   - scene to create billboard
 *   PARAM: [IN] textureID - billboard texture
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEBillboardCreate( const VEUINT sceneID, const VEUINT textureID )
{
  VEBILLBOARD *billboard = 0;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
    return 0;

  /* Billboard creation */
  billboard = New(sizeof(VEBILLBOARD), "Billboard");
  if (!billboard)
    return 0;

  /* Setup parameters */
  billboard->m_Size = 1.0;
  billboard->m_TextureID = textureID;

  /* Bind shader parameters */
  billboard->m_ShaderParameters = VEShaderParametersBind(p_SettingsShaderBillboard[VE_SHADER_PROGRAM]);

  /* That's it */
  return VEInternalContainerAdd(scene->m_Billboards, billboard);
} /* End of 'VEBillboardCreate' function */

/***
 * PURPOSE: Set billboard's position
 *   PARAM: [IN] sceneID     - scene identifier
 *   PARAM: [IN] billboardID - billboard's identifier
 *   PARAM: [IN] position    - new billboard's position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBillboardSetPosition( const VEUINT sceneID, const VEUINT billboardID, const VEVECTOR3D position )
{
  VEBILLBOARD *billboard = 0;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
    return;

  billboard = VEInternalContainerGet(scene->m_Billboards, billboardID);
  if (!billboard)
    return;

  /* Setup new billboard position */
  billboard->m_Position = position;
} /* End of 'VEBillboardSetPosition' function */

/***
 * PURPOSE: Set billboard's size
 *   PARAM: [IN] sceneID     - scene identifier
 *   PARAM: [IN] billboardID - billboard's identifier
 *   PARAM: [IN] size        - new billboard's size
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBillboardSetSize( const VEUINT sceneID, const VEUINT billboardID, const VEREAL size )
{
  VEBILLBOARD *billboard = 0;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
    return;

  /* Find billboard */
  billboard = VEInternalContainerGet(scene->m_Billboards, billboardID);
  if (!billboard)
    return;

  /* Setup new billboard size */
  billboard->m_Size = size;
} /* End of 'VEBillboardSetSize' function */

/***
 * PURPOSE: Deletes existing billboard
 *   PARAM: [IN] sceneID     - scene identifier
 *   PARAM: [IN] billboardID - billboard's identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBillboardDelete( const VEUINT sceneID, const VEUINT billboardID )
{
  VEBILLBOARD *billboard = 0;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
    return;

  /* Find billboard */
  billboard = VEInternalContainerGet(scene->m_Billboards, billboardID);
  if (!billboard)
    return;

  /* Remove billboard */
  VEInternalContainerRemove(scene->m_Billboards, billboardID);
  Delete(billboard);
} /* End of 'VEBillboardDelete' function */

/***
 * PURPOSE: Render billboard
 *   PARAM: [IN] billboard - pointer to billboard
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBillboardRender( VEBILLBOARD *billboard )
{


} /* End of 'VEBillboardRender' function */
