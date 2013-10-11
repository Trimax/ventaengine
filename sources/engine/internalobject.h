#ifndef INTERNALOBJECT_H_INCLUDED
#define INTERNALOBJECT_H_INCLUDED

#include "internalboundsphere.h"
#include "internalmaterial.h"
#include "internalmesh.h"
#include "types.h"

#include <stdio.h>

/* Object's current animation */
typedef struct tagVEOBJECTANIMATION
{
  VEUINT m_FrameStart;     /* Startup animation frame */
  VEUINT m_FrameEnd;       /* End animation frame */
  VEREAL m_TimeFactor;     /* Animation time factor */
  VEBOOL m_IsLooped;       /* Animation loop flag  */

  VEUINT m_FrameID;        /* Current frame */
  VEREAL m_FramePercent;   /* Frame percent */
  VEREAL m_LocalTime;      /* Object's local time */
  VEQUATERNION m_Rotation; /* Current rotation */
} VEOBJECTANIMATION;

/* Object */
typedef struct tagVEOBJECT
{
  VEUINT            m_ID;             /* Self identifier */
  VEUINT            m_NumFrames;      /* Number of animation frames */
  VEUINT            m_FrameRate;      /* Frames per second */

  VEUINT            m_NumMeshes;      /* Number of meshes */
  VEMESH          **m_Meshes;         /* Meshes array */

  VEUINT            m_NumMaterials;   /* Number of materials */
  VEMATERIAL      **m_Materials;      /* Materials array */

  /* Equation */
  VEVECTOR3D        m_Position;       /* Object's position */
  VEVECTOR3D        m_Rotation;       /* Object's rotation */
  VEVECTOR3D        m_Scaling;        /* Object's scaling */

  /* First derivative */
  VEVECTOR3D        m_VelPosition;    /* Object's 3D velocity */
  VEVECTOR3D        m_VelRotation;    /* Object's rotation velocity */
  VEVECTOR3D        m_VelScaling;     /* Object's scaling velocity */

  VEREAL            m_Mass;           /* Object's mass */
  VEREAL            m_Temperature;    /* Object's temperature */
  VEREAL            m_Charge;         /* Object's charge */

  VEREAL            m_EventHorizont;  /* Object's event horizont */

  VEVECTOR3D        m_Force;          /* Force, applied to object */

  VEBOUNDINGSPHERE  m_BoundingSphere; /* Object's bounding sphere */

  VEOBJECTANIMATION m_Animation;      /* Object's animation parameters */
} VEOBJECT;

/***
 * PURPOSE: Load object from a file
 *  RETURN: Pointer to object if success, NULL otherwise
 *   PARAM: [IN] f - pointer to file stream
 *  AUTHOR: Eliseev Dmitry
 ***/
VEOBJECT *VEObjectLoadInternal( FILE *f );

/***
 * PURPOSE: Calculate object's bounding sphere
 *  RETURN: Object bounding sphere
 *   PARAM: [IN] object - pointer to object to build sphere
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOUNDINGSPHERE VEObjectBoundingSphereCreateInternal( const VEOBJECT *object );

/***
 * PURPOSE: Apply shaders program to object
 *   PARAM: [IN] object    - object to apply program
 *   PARAM: [IN] programID - shaders program to apply
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectApplyProgramInternal( VEOBJECT *object, const VEUINT programID );

/***
 * PURPOSE: Determine object's intersection using bounding spheres
 *  RETURN: TRUE if objects has intersection, FALSE otherwise
 *   PARAM: [IN] obj1 - pointer to object 1
 *   PARAM: [IN] obj1 - pointer to object 2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEObjectsIntersects( const VEOBJECT *obj1, const VEOBJECT *obj2 );

/***
 * PURPOSE: Delete object
 *   PARAM: [IN] object - pointer to object to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectDeleteInternal( VEOBJECT *object );

/***
 * PURPOSE: Render object
 *   PARAM: [IN] object - pointer to object to render
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectRender( VEOBJECT *object );

/***
 * PURPOSE: Update object
 *   PARAM: [IN] object      - pointer to object to render
 *   PARAM: [IN] timeElapsed - pointer to variable, contains elapsed time since last update
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectUpdate( VEOBJECT *object, VEREAL *timeElapsed );

/***
 * PURPOSE: Print objects information to console
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEObjectList( VEVOID );

#endif // INTERNALOBJECT_H_INCLUDED
