#ifndef SCENEMANAGER_H_INCLUDED
#define SCENEMANAGER_H_INCLUDED

/***
 * PURPOSE: Create a new scene
 *  RETURN: Scene identifier if success, 0 otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VESceneCreate( VEVOID );

/***
 * PURPOSE: Select scene to render
 *   PARAM: [IN] sceneID - scene identifier to render
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESceneSelect( const VEUINT sceneID );

/***
 * PURPOSE: Delete scene
 *   PARAM: [IN] sceneID - scene identifier to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESceneDelete( const VEUINT sceneID );

#endif // SCENEMANAGER_H_INCLUDED
