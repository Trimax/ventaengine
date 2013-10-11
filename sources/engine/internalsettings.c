#include "internalglut.h"

#include "internalsettings.h"

/*** Graphics settings ***/

/* Axes rendering */
VEBOOL p_SettingsGraphicsRenderAxes = FALSE;

/* Forces rendering */
VEBOOL p_SettingsGraphicsRenderForces = FALSE;

/* Lights rendering */
VEBOOL p_SettingsGraphicsRenderLights = FALSE;

/* Cameras rendering */
VEBOOL p_SettingsGraphicsRenderCameras = FALSE;

/* VBO extension supported */
VEBOOL p_SettingsGraphicsVBOSupported = FALSE;

/* VE configuration */
VECONFIGURATION p_SettingsConfiguration;

/* Current scene identifier */
VEUINT p_SettingsGraphicsSceneID = 0;

/* Current interface identifier */
VEUINT p_SettingsGraphicsInterfaceID = 0;

/* Rendering type (solid/wire/points) */
VEBYTE p_SettingsGraphicsRenderType = GL_TRIANGLES;

/* Slow primitives rendering */
VEBOOL p_SettingsGraphicsRenderSlow = FALSE;

/* Is shaders enabled */
VEBOOL p_SettingsGraphicsShaders = TRUE;

/* Rendering critical section */
VECRITICALSECTION *p_SettingsGraphicsSection = NULL;

/* Frustum planes */
VEREAL p_SettingsGraphicsFrustum[6][4];

/*** Physics settings ***/

/* Gravity */
VEBOOL p_SettingsGravityEnabled = FALSE;

/* Current local time */
VEREAL p_SettingsTime = 0.0;

/*** Common settings ***/

/* Engine started timestamp */
VEULONG p_SettingsEngineStarted = 0;

/*** Predefined shaders ***/

/* Render shaders & linked program */
VEUINT p_SettingsShaderRender[3] = {0, 0, 0};

/* Normal mapping shaders & linked program */
VEUINT p_SettingsShaderNormalMapping[3] = {0, 0, 0};

/* Planet shaders & linked program */
VEUINT p_SettingsShaderPlanet[3] = {0, 0, 0};

/* Terrain shaders & linked program */
VEUINT p_SettingsShaderTerrain[3] = {0, 0, 0};

/* Cartoon shaders & linked program */
VEUINT p_SettingsShaderCartoon[3] = {0, 0, 0};

/* Billboard shaders & linked program */
VEUINT p_SettingsShaderBillboard[3] = {0, 0, 0};

/* Environment shaders & linked program */
VEUINT p_SettingsShaderEnvironment[3] = {0, 0, 0};
