#include "internalresourcemanager.h"
#include "internalshadermanager.h"
#include "internalshadermanager.h"
#include "internalscenemanager.h"
#include "internalcontainer.h"
#include "internalobject.h"
#include "shadermanager.h"
#include "console.h"
#include "object.h"

#ifdef _WIN32
#include <gl/glew.h>
#else
#define GL_GLEXT_PROTOTYPES
#include <GL/gl.h>
#include "gl/glext.h"
#endif

#include <string.h>

/***
 * PURPOSE: Create shader parameters structure by linked program identifier
 *  RETURN: Shader parameters structure
 *   PARAM: [IN] programID - shaders program ID to bind
 *  AUTHOR: Eliseev Dmitry
 ***/
VESHADERINTERNAL VEShaderParametersBind( const VEUINT programID )
{
  VESHADERINTERNAL shaderParameters;
  memset(&shaderParameters, 0, sizeof(VESHADERINTERNAL));

  /* Maps binding */
  shaderParameters.m_MapDiffuse        = glGetUniformLocation(programID, VE_SHADERPARAM_MAPDIFFUSE);
  shaderParameters.m_MapNormals        = glGetUniformLocation(programID, VE_SHADERPARAM_MAPNORMALS);
  shaderParameters.m_MapReflection     = glGetUniformLocation(programID, VE_SHADERPARAM_MAPREFLECTION);
  shaderParameters.m_MapOpacity        = glGetUniformLocation(programID, VE_SHADERPARAM_MAPOPACITY);

  /* Maps flags binding */
  shaderParameters.m_MapDiffuseFlag    = glGetUniformLocation(programID, VE_SHADERPARAM_MAPDIFFUSEFLAG);
  shaderParameters.m_MapNormalsFlag    = glGetUniformLocation(programID, VE_SHADERPARAM_MAPNORMALSFLAG);
  shaderParameters.m_MapReflectionFlag = glGetUniformLocation(programID, VE_SHADERPARAM_MAPREFLECTIONFLAG);
  shaderParameters.m_MapOpacityFlag    = glGetUniformLocation(programID, VE_SHADERPARAM_MAPOPACITYFLAG);

  /* Camera position */
  shaderParameters.m_CameraPosition    = glGetUniformLocation(programID, VE_SHADERPARAM_CAMERAPOS);

  /* Sun position */
  shaderParameters.m_SunPosition      = glGetUniformLocation(programID, VE_SHADERPARAM_SUNPOSITION);

  /* Sky color */
  shaderParameters.m_SkyColor         = glGetUniformLocation(programID, VE_SHADERPARAM_SKYCOLOR);

  /* Lights state */
  shaderParameters.m_Lights           = glGetUniformLocation(programID, VE_SHADERPARAM_LIGHTS);

  /* That's it */
  return shaderParameters;
} /* End of 'VEShaderParametersBind' function */

/***
 * PURPOSE: Apply shaders program to object
 *   PARAM: [IN] object    - object to apply program
 *   PARAM: [IN] programID - shaders program to apply
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectApplyProgramInternal( VEOBJECT *object, const VEUINT programID )
{
  VEUINT meshID = 0;

  /* Bind program */
  glUseProgram(programID);

  for (meshID = 0; meshID < object->m_NumMeshes; meshID++)
  {
    /* Store linked program identifier */
    object->m_Meshes[meshID]->m_ProgramID = programID;

    /* Bind variables */
    object->m_Meshes[meshID]->m_ShaderParameters = VEShaderParametersBind(programID);
  }

  /* Unbind program */
  glUseProgram(0);
} /* End of 'VEObjectApplyProgramInternal' function */

/***
 * PURPOSE: Load object from a file (either from resources or from FS)
 *  RETURN: Object identifier if success, 0 otherwise
 *   PARAM: [IN] sceneID  - scene identifier to load object
 *   PARAM: [IN] filename - file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEObjectLoad( const VEUINT sceneID, const VEBUFFER filename )
{
  VEOBJECT *object = NULL;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return 0;
  }

  /* Open object's file */
  VEFILE *f = VEResourcePtrOpen(filename);
  if (!f)
  {
    VEConsolePrintArg("Object not found: ", filename);
    return 0;
  }

  /* Load object from file */
  object = VEObjectLoadInternal(f->m_Ptr);

  /* Close file */
  VEResourcePtrClose(f);

  /* Wrong object */
  if (!object)
  {
    VEConsolePrintArg("Can't load object: ", filename);
    return 0;
  }

  /* Default object scaling */
  object->m_Scaling = VEVector3D(1.0, 1.0, 1.0);

  /* Calculate bounding sphere volume */
  object->m_BoundingSphere = VEObjectBoundingSphereCreateInternal(object);

  /* Apply default shader */
  VEObjectApplyProgramInternal(object, VEProgramGet(VE_PROGRAM_RENDER));

  /* Add object to scene */
  object->m_ID = VEInternalContainerAdd(scene->m_Objects, object);
  return object->m_ID;
} /* End of 'VEObjectLoad' function */

/***
 * PURPOSE: Delete object
 *   PARAM: [IN] sceneID  - scene identifier to delete object
 *   PARAM: [IN] objectID - object identifier to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectDelete( const VEUINT sceneID, const VEUINT objectID )
{
  VEOBJECT *object = NULL;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return;
  }

  /* Remove object from scene */
  object = VEInternalContainerGet(scene->m_Objects, objectID);
  VEInternalContainerRemove(scene->m_Objects, objectID);

  /* Delete object */
  VEObjectDeleteInternal(object);
} /* End of 'VEObjectDelete' function */

/***
 * PURPOSE: Position object in a space
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier to position
 *   PARAM: [IN] position - new object's position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectSetPosition( const VEUINT sceneID, const VEUINT objectID, const VEVECTOR3D position )
{
  VEOBJECT *object = NULL;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return;
  }

  /* Remove object from scene */
  object = VEInternalContainerGet(scene->m_Objects, objectID);
  if (object)
    object->m_Position = position;
  else
    VEConsolePrint("Object doesn't exist");
} /* End of 'VEObjectSetPosition' function */

/***
 * PURPOSE: Set object velocity
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier to position
 *   PARAM: [IN] velocity - new object's velocity
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectSetVelocity( const VEUINT sceneID, const VEUINT objectID, const VEVECTOR3D velocity )
{
  VEOBJECT *object = NULL;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return;
  }

  /* Remove object from scene */
  object = VEInternalContainerGet(scene->m_Objects, objectID);
  if (object)
    object->m_VelPosition = velocity;
  else
    VEConsolePrint("Object doesn't exist");
} /* End of 'VEObjectSetPosition' function */

/***
 * PURPOSE: Rotate object in a space
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier to position
 *   PARAM: [IN] rotation - new object's rotation
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectSetRotation( const VEUINT sceneID, const VEUINT objectID, const VEVECTOR3D rotation )
{
  VEOBJECT *object = NULL;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return;
  }

  /* Remove object from scene */
  object = VEInternalContainerGet(scene->m_Objects, objectID);
  if (object)
    object->m_Rotation = rotation;
  else
    VEConsolePrint("Object doesn't exist");
} /* End of 'VEObjectSetRotation' function */

/***
 * PURPOSE: Scale object in a space
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier to position
 *   PARAM: [IN] scaling  - new object's scaling
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectSetScaling( const VEUINT sceneID, const VEUINT objectID, const VEVECTOR3D scaling )
{
  VEOBJECT *object = NULL;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return;
  }

  /* Remove object from scene */
  object = VEInternalContainerGet(scene->m_Objects, objectID);
  if (object)
    object->m_Scaling = scaling;
  else
    VEConsolePrint("Object doesn't exist");
} /* End of 'VEObjectSetRotation' function */

/***
 * PURPOSE: Apply shaders program to object
 *   PARAM: [IN] sceneID   - scene identifier where is object
 *   PARAM: [IN] objectID  - object identifier to apply program
 *   PARAM: [IN] programID - shaders program to apply
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectApplyProgram( const VEUINT sceneID, const VEUINT objectID, const VEUINT programID )
{
  VEOBJECT *object = NULL;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return;
  }

  /* Remove object from scene */
  object = VEInternalContainerGet(scene->m_Objects, objectID);
  if (!object)
  {
    VEConsolePrint("Object doesn't exist");
    return;
  }

  /* Apply program */
  VEObjectApplyProgramInternal(object, programID);
} /* End of 'VEObjectApplyProgram' function */

/***
 * PURPOSE: Enable object animation
 *   PARAM: [IN] sceneID    - scene identifier where is object
 *   PARAM: [IN] objectID   - object identifier to apply program
 *   PARAM: [IN] startFrame - startup object's frame
 *   PARAM: [IN] endFrame   - end object's frame
 *   PARAM: [IN] timeFactor - time multiplier
 *   PARAM: [IN] isLooped   - animation loop flag
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectAnimate( const VEUINT sceneID, const VEUINT objectID, const VEUINT startFrame, const VEUINT endFrame, const VEREAL timeFactor, const VEBOOL isLooped )
{
  VEOBJECT *object = NULL;
  VESCENE *scene = VESceneGet(sceneID);
  if (!scene)
  {
    VEConsolePrint("Scene doesn't exist");
    return;
  }

  /* Remove object from scene */
  object = VEInternalContainerGet(scene->m_Objects, objectID);
  if (!object)
  {
    VEConsolePrint("Object doesn't exist");
    return;
  }

  /* Store object's animation parameters */
  object->m_Animation.m_FrameStart = startFrame;
  object->m_Animation.m_FrameEnd   = endFrame;
  object->m_Animation.m_TimeFactor = timeFactor;
  object->m_Animation.m_IsLooped   = isLooped;
} /* End of 'VEObjectAnimate' function */
