#ifndef SHADERMANAGER_H_INCLUDED
#define SHADERMANAGER_H_INCLUDED

#include "types.h"

/*** Shader programs types ***/

/* Vertex program */
#define VE_SHADER_VERTEX 0

/* Fragment program */
#define VE_SHADER_FRAGMENT 1

/*** Predefined shaders ***/

/* Default rendering shader */
#define VE_PROGRAM_RENDER 1

/* Normal mapping */
#define VE_PROGRAM_NORMALMAPPING 2

/* Planet with atmosphere shader */
#define VE_PROGRAM_PLANET 3

/***
 * PURPOSE: Select scene to render
 *  RETURN: Program identifier if success, 0 otherwise
 *   PARAM: [IN] type       - shader program type
 *   PARAM: [IN] shaderFile - shader file
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEShaderLoad( const VEBYTE type, const VEBUFFER shaderFile );

/***
 * PURPOSE: Delete shader
 *   PARAM: [IN] shaderID - created shader identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEShaderDelete( const VEUINT shaderID );

/***
 * PURPOSE: Create program using shaders
 *  RETURN: Program identifier if success, 0 otherwise
 *   PARAM: [IN] shaders    - array of shaders
 *   PARAM: [IN] numShaders - number of shaders in array
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEProgramCreate( const VEUINT *shaders, const VEUINT numShaders );

/***
 * PURPOSE: Get identifier of predefined program
 *  RETURN: Predefined program identifier if success, 0 otherwise
 *   PARAM: [IN] predefinedProgramType - predefined program type (one of VE_PROGRAM_XXX definitions)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEProgramGet( const VEUINT predefinedProgramType );

/***
 * PURPOSE: Delete program
 *   PARAM: [IN] programID - created program identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEProgramDelete( const VEUINT programID );

#endif // SHADERMANAGER_H_INCLUDED
