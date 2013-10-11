#ifndef INTERNALSHADERMANAGER_H_INCLUDED
#define INTERNALSHADERMANAGER_H_INCLUDED

#include "internalscenemanager.h"
#include "types.h"

/*** VE Environment parameters ***/

/** Maps **/

/* Diffuse map */
#define VE_SHADERPARAM_MAPDIFFUSE        "veMapDiffuse"
#define VE_SHADERPARAM_MAPDIFFUSEFLAG    "veMapDiffuseFlag"

/* Normals map */
#define VE_SHADERPARAM_MAPNORMALS        "veMapNormals"
#define VE_SHADERPARAM_MAPNORMALSFLAG    "veMapNormalsFlag"

/* Reflection map */
#define VE_SHADERPARAM_MAPREFLECTION     "veMapReflection"
#define VE_SHADERPARAM_MAPREFLECTIONFLAG "veMapReflectionFlag"

/* Opacity map */
#define VE_SHADERPARAM_MAPOPACITY        "veMapOpacity"
#define VE_SHADERPARAM_MAPOPACITYFLAG    "veMapOpacityFlag"

/** Terrain maps **/

/* Blend map */
#define VE_SHADERPARAM_MAPBLEND      "veMapBlend"

/* Map #1 */
#define VE_SHADERPARAM_MAP1          "veMap1"

/* Map #2 */
#define VE_SHADERPARAM_MAP2          "veMap2"

/* Map #3 */
#define VE_SHADERPARAM_MAP3          "veMap3"

/* Map #4 */
#define VE_SHADERPARAM_MAP4          "veMap4"

/** Terrain parameters **/
#define VE_SHADERPARAM_PARAMETERS    "veParameters"

/** Camera **/

/* Position */
#define VE_SHADERPARAM_CAMERAPOS     "veCameraPosition"

/** Lights **/

/* State */
#define VE_SHADERPARAM_LIGHTS        "veLights"

/** Mesh **/

/* Tangents */
#define VE_SHADERPARAM_TANGENTS      "veTangent"

/* Binormals */
#define VE_SHADERPARAM_BINORMALS     "veBinormal"

/** Environment parameters **/

/* Sun position */
#define VE_SHADERPARAM_SUNPOSITION   "veSunPosition"

/* Sky color */
#define VE_SHADERPARAM_SKYCOLOR      "veSkyColor"

/***
 * PURPOSE: Build predefined shaders
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEShaderInitInternal( VEVOID );

/***
 * PURPOSE: Build predefined shaders
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEShaderDeinitInternal( VEVOID );

/***
 * PURPOSE: Create a new shader program using shader's code
 *  RETURN: Shader identifier if success, 0 otherwise
 *   PARAM: [IN] type       - shader program type
 *   PARAM: [IN] strShader  - shader code
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEShaderCreateInternal( VEUINT eShaderType, VEBUFFER strShader );

/***
 * PURPOSE: Load shader program from file
 *  RETURN: Shader identifier if success, 0 otherwise
 *   PARAM: [IN] type       - shader program type
 *   PARAM: [IN] shaderFile - shader file
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEShaderLoadInternal( VEUINT eShaderType, VEBUFFER fileName );

/***
 * PURPOSE: Use program and bind uniform parameters
 *   PARAM: [IN] mesh - pointer to mesh
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEProgramUseInternal( const VEMESH *mesh );

/***
 * PURPOSE: Create shader parameters structure by linked program identifier
 *  RETURN: Shader parameters structure
 *   PARAM: [IN] programID - shaders program ID to bind
 *  AUTHOR: Eliseev Dmitry
 ***/
VESHADERINTERNAL VEShaderParametersBind( const VEUINT programID );

#endif // INTERNALSHADERMANAGER_H_INCLUDED
