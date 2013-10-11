#ifndef INTERNALSCENEMANAGER_H_INCLUDED
#define INTERNALSCENEMANAGER_H_INCLUDED

#include "internalenvironment.h"
#include "internalcontainer.h"
#include "internalterrain.h"
#include "internallight.h"

/* Number of allowed lights per scene */
#define VE_DEFAULT_LIGHTS 10

/* Scene type definition */
typedef struct tagVESCENE
{
  VEUINT               m_ID;          /* Self identifier */
  VEUINT               m_CameraID;    /* Current camera identfier */

  VETERRAIN           *m_Terrain;     /* Scene terrain */
  VEENVIRONMENT       *m_Environment; /* Scene environment */

  VEINTERNALCONTAINER *m_Cameras;     /* Scene cameras */
  VEINTERNALCONTAINER *m_Objects;     /* Scene objects */
  VEINTERNALCONTAINER *m_Lights;      /* Scene lights */
  VEINTERNALCONTAINER *m_Emitters;    /* Scene emitters */
  VEINTERNALCONTAINER *m_Billboards;  /* Scene billboards */

  VEINT                m_LightStates[VE_MAXIMAL_LIGHTS+1]; /* Light states */
} VESCENE;

/***
 * PURPOSE: Initialize scene manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VESceneInit( VEVOID );

/***
 * PURPOSE: Get scene pointer by it's identifier
 *  RETURN: Scene pointer if success, NULL otherwise
 *   PARAM: [IN] sceneID - scene identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VESCENE *VESceneGet( const VEUINT sceneID );

/***
 * PURPOSE: Render current scene
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESceneRender( VEVOID );

/***
 * PURPOSE: Update current scene
 *   PARAM: [IN] timeElapsed - time since last frame rendered
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESceneUpdate( const VEREAL timeElapsed );

/***
 * PURPOSE: Deinitialize scene manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESceneDeinit( VEVOID );

/***
 * PURPOSE: Print scenes information to console
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESceneList( VEVOID );

/***
 * PURPOSE: Set current camera parameters
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESceneSetCamera( VEVOID );

#endif // INTERNALSCENEMANAGER_H_INCLUDED
