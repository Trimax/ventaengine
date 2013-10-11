#ifndef OBJECTMANAGER_H_INCLUDED
#define OBJECTMANAGER_H_INCLUDED

#include "vector3d.h"

/***
 * PURPOSE: Load object from a file (either from resources or from FS)
 *  RETURN: Object identifier if success, 0 otherwise
 *   PARAM: [IN] sceneID  - scene identifier to load object
 *   PARAM: [IN] filename - file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEObjectLoad( const VEUINT sceneID, const VEBUFFER filename );

/***
 * PURPOSE: Position object in a space
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier to position
 *   PARAM: [IN] position - new object's position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectSetPosition( const VEUINT sceneID, const VEUINT objectID, const VEVECTOR3D position );

/***
 * PURPOSE: Set object velocity
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier to position
 *   PARAM: [IN] velocity - new object's velocity
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectSetVelocity( const VEUINT sceneID, const VEUINT objectID, const VEVECTOR3D velocity );

/***
 * PURPOSE: Rotate object in a space
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier to position
 *   PARAM: [IN] rotation - new object's rotation
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectSetRotation( const VEUINT sceneID, const VEUINT objectID, const VEVECTOR3D rotation );

/***
 * PURPOSE: Scale object in a space
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier to position
 *   PARAM: [IN] scaling  - new object's scaling
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectSetScaling( const VEUINT sceneID, const VEUINT objectID, const VEVECTOR3D scaling );

/***
 * PURPOSE: Apply shaders program to object
 *   PARAM: [IN] sceneID   - scene identifier where is object
 *   PARAM: [IN] objectID  - object identifier to apply program
 *   PARAM: [IN] programID - shaders program to apply
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectApplyProgram( const VEUINT sceneID, const VEUINT objectID, const VEUINT programID );

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
VEVOID VEObjectAnimate( const VEUINT sceneID, const VEUINT objectID, const VEUINT startFrame, const VEUINT endFrame, const VEREAL timeFactor, const VEBOOL isLooped );

/***
 * PURPOSE: Delete object
 *   PARAM: [IN] sceneID  - scene identifier where is object
 *   PARAM: [IN] objectID - object identifier to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectDelete( const VEUINT sceneID, const VEUINT objectID );

#endif // OBJECTMANAGER_H_INCLUDED
