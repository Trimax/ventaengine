#ifndef INTERNALSETTINGS_H_INCLUDED
#define INTERNALSETTINGS_H_INCLUDED

#include "internalcriticalsection.h"
#include "internalstring.h"
#include "configuration.h"

/*** Definitions ***/

/* Linked program index */
#define VE_SHADER_PROGRAM 2

/*** Common settings ***/
extern VECONFIGURATION p_SettingsConfiguration;

/* Engine started timestamp */
extern VEULONG p_SettingsEngineStarted;

/*** Graphics settings ***/

/* Axes rendering */
extern VEBOOL p_SettingsGraphicsRenderAxes;

/* Forces rendering */
extern VEBOOL p_SettingsGraphicsRenderForces;

/* Light rendering state */
extern VEBOOL p_SettingsGraphicsRenderLights;

/* Cameras rendering */
extern VEBOOL p_SettingsGraphicsRenderCameras;

/* VBO extension supported */
extern VEBOOL p_SettingsGraphicsVBOSupported;

/* Debug SLOW rendering */
extern VEBOOL p_SettingsGraphicsRenderSlow;

/* Is shaders enabled */
extern VEBOOL p_SettingsGraphicsShaders;

/* Current scene identifier */
extern VEUINT p_SettingsGraphicsSceneID;

/* Current interface identifier */
extern VEUINT p_SettingsGraphicsInterfaceID;

/* Rendering type (solid/wire/points) */
extern VEBYTE p_SettingsGraphicsRenderType;

/* Rendering critical section */
extern VECRITICALSECTION *p_SettingsGraphicsSection;

/* Frustum planes */
extern VEREAL p_SettingsGraphicsFrustum[6][4];

/*** Physics settings ***/

/* Gravity */
extern VEBOOL p_SettingsGravityEnabled;

/* Current local time */
extern VEREAL p_SettingsTime;

/*** Predefined shaders ***/

/* Normal mapping linked program */
extern VEUINT p_SettingsShaderRender[3];

/* Normal mapping linked program */
extern VEUINT p_SettingsShaderNormalMapping[3];

/* Planet shaders & linked program */
extern VEUINT p_SettingsShaderPlanet[3];

/* Terrain shaders & linked program */
extern VEUINT p_SettingsShaderTerrain[3];

/* Cartoon shaders & linked program */
extern VEUINT p_SettingsShaderCartoon[3];

/* Billboard shaders & linked program */
extern VEUINT p_SettingsShaderBillboard[3];

/* Environment shaders & linked program */
extern VEUINT p_SettingsShaderEnvironment[3];

#endif // INTERNALSETTINGS_H_INCLUDED
