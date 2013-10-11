#ifndef VECTOR3D_H_INCLUDED
#define VECTOR3D_H_INCLUDED

#include "types.h"

/* 3D vector definition */
typedef struct tagVEVECTOR3D
{
  VEREAL m_X; /* Absciss */
  VEREAL m_Y; /* Ordinate */
  VEREAL m_Z; /* Applicate */
} VEVECTOR3D;

/***
 * PURPOSE: Create a new vector by components
 *  RETURN: Created vector
 *   PARAM: [IN] x - absciss
 *   PARAM: [IN] y - ordinate
 *   PARAM: [IN] z - applicate
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3D( const VEREAL x, const VEREAL y, const VEREAL z );

/***
 * PURPOSE: Add two 3D vectors
 *  RETURN: Summ vector
 *   PARAM: [IN] v1 - first part
 *   PARAM: [IN] v2 - second part
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DAdd( const VEVECTOR3D v1, const VEVECTOR3D v2 );

/***
 * PURPOSE: Diff two 3D vectors
 *  RETURN: Summ vector
 *   PARAM: [IN] v1 - first part
 *   PARAM: [IN] v2 - second part
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DSub( const VEVECTOR3D v1, const VEVECTOR3D v2 );

/***
 * PURPOSE: Mix two 3D vectors according to formula: alpha*v1 + (1-alpha)*v2
 *  RETURN: Mixed vector
 *   PARAM: [IN] v1    - first part
 *   PARAM: [IN] v2    - second part
 *   PARAM: [IN] alpha - mixing coefficient (alpha must be from [0; 1.0])
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DMix( const VEVECTOR3D v1, const VEVECTOR3D v2, const VEREAL alpha );

/***
 * PURPOSE: Multiply vector with a number
 *  RETURN: Multiplied vector
 *   PARAM: [IN] v - vector to multiply
 *   PARAM: [IN] n - number
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DMult( const VEVECTOR3D v, const VEREAL n );

/***
 * PURPOSE: Cross product of two vectors
 *  RETURN: Cross product of two vectors
 *   PARAM: [IN] v1 - left vector
 *   PARAM: [IN] v2 - right vector
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DCross( const VEVECTOR3D v1, const VEVECTOR3D v2 );

/***
 * PURPOSE: Dot product of two vectors
 *  RETURN: Dot product of two vectors
 *   PARAM: [IN] v1 - left vector
 *   PARAM: [IN] v2 - right vector
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEVector3DDot( const VEVECTOR3D v1, const VEVECTOR3D v2 );

/***
 * PURPOSE: Determine vector norm
 *  RETURN: Vector's norm
 *   PARAM: [IN] v - vector to determine norm
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEVector3DNorm( const VEVECTOR3D v );

/***
 * PURPOSE: Normalize vector
 *  RETURN: Normalized vector
 *   PARAM: [IN] v - vector to normalize
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DNormalize( const VEVECTOR3D v );

/***
 * PURPOSE: Euclid distance between two points
 *  RETURN: Euclid distance
 *   PARAM: [IN] v1 - point 1
 *   PARAM: [IN] v2 - point 2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEVector3DDistance( const VEVECTOR3D v1, const VEVECTOR3D v2 );

/***
 * PURPOSE: Euclid distance square between two points
 *  RETURN: Euclid distance square
 *   PARAM: [IN] v1 - point 1
 *   PARAM: [IN] v2 - point 2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEVector3DDistanceSquare( const VEVECTOR3D v1, const VEVECTOR3D v2 );

#endif // VECTOR3D_H_INCLUDED
