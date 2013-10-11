#ifndef LIGHT_H_INCLUDED
#define LIGHT_H_INCLUDED

#include "vector3d.h"
#include "color.h"

/*** Light parameters ***/
typedef struct tagVELIGHTPARAMETERS
{
  VECOLOR m_Ambient;   /* Light color ambient component */
  VECOLOR m_Diffuse;   /* Light color diffuse component */
  VECOLOR m_Specular;  /* Light color specular component */
  VEREAL  m_Shineness; /* Light color shineness component */
} VELIGHTPARAMETERS;

/***
 * PURPOSE: Create a new light source
 *  RETURN: Created light identifier
 *   PARAM: [IN] sceneID - scene identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VELightCreate( const VEUINT sceneID );

/***
 * PURPOSE: Set light position
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] lightID  - existing light identifier
 *   PARAM: [IN] position - new light position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightSetPosition( const VEUINT sceneID, const VEUINT lightID, const VEVECTOR3D position );

/***
 * PURPOSE: Set light direction (for spot lights only)
 *   PARAM: [IN] sceneID   - scene identifier
 *   PARAM: [IN] lightID   - existing light identifier
 *   PARAM: [IN] direction - new light direction
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightSetDirection( const VEUINT sceneID, const VEUINT lightID, const VEVECTOR3D direction );

/***
 * PURPOSE: Set light parameters
 *   PARAM: [IN] sceneID    - scene identifier
 *   PARAM: [IN] lightID    - existing light identifier
 *   PARAM: [IN] parameters - new light color position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightSetParameters( const VEUINT sceneID, const VEUINT lightID, const VELIGHTPARAMETERS parameters );

/***
 * PURPOSE: Enable light
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] lightID  - existing light identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightEnable( const VEUINT sceneID, const VEUINT lightID );

/***
 * PURPOSE: Disable light
 *   PARAM: [IN] sceneID  - scene identifier
 *   PARAM: [IN] lightID  - existing light identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightDisable( const VEUINT sceneID, const VEUINT lightID );

/***
 * PURPOSE: Delete an existing light
 *   PARAM: [IN] sceneID - scene identifier
 *   PARAM: [IN] lightID - light identifier to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightDelete( const VEUINT sceneID, const VEUINT lightID );

#endif // LIGHT_H_INCLUDED
