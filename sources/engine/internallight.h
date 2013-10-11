#ifndef INTERNALLIGHTMANAGER_H_INCLUDED
#define INTERNALLIGHTMANAGER_H_INCLUDED

#include "vector4d.h"
#include "light.h"

/* Maximal number of lights */
#define VE_MAXIMAL_LIGHTS 8

/* Largest light number. Must be string and equal to VE_MAXIMAL_LIGHTS+1 */
#define VE_MAXIMAL_LIGHTS_SHADER "9"

/* Light structure */
typedef struct tagVELIGHT
{
  VEUINT     m_ID;         /* Self ID */
  VEVECTOR4D m_Position;   /* Light position */
  VEVECTOR4D m_Direction;  /* Spot light direction */

  VEVECTOR4D m_Ambient;    /* Light ambient color */
  VEVECTOR4D m_Diffuse;    /* Light diffuse color */
  VEVECTOR4D m_Specular;   /* Light specular color */
  VEREAL     m_Shininess;  /* Light shininess size */

  VEREAL     m_Cutoff;     /* Spot light cutoff */
  VEBOOL     m_IsEnabled;  /* Is light enabled */

  VEUINT     m_LightID;    /* OpenGL light ID */
} VELIGHT;

/***
 * PURPOSE: Render all lights of scene as spheres
 *   PARAM: [IN] sceneID - scene identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightRender( const VEUINT sceneID );

/***
 * PURPOSE: Apply light settings
 *   PARAM: [IN] light - pointer to light to apply
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightApplyInternal( const VELIGHT *light );

/***
 * PURPOSE: Print camera list to console
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VELightList( VEVOID );

#endif // INTERNALLIGHTMANAGER_H_INCLUDED
