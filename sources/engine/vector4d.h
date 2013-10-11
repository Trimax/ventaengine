#ifndef VECTOR4D_H_INCLUDED
#define VECTOR4D_H_INCLUDED

#include "vector3d.h"
#include "types.h"

/* 3D vector definition */
typedef struct tagVEVECTOR4D
{
  VEREAL m_X; /* Absciss */
  VEREAL m_Y; /* Ordinate */
  VEREAL m_Z; /* Applicate */
  VEREAL m_W; /* Additional */
} VEVECTOR4D;

/***
 * PURPOSE: Create a new vector by components
 *  RETURN: Created vector
 *   PARAM: [IN] x - absciss
 *   PARAM: [IN] y - ordinate
 *   PARAM: [IN] z - applicate
 *   PARAM: [IN] w - additional
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR4D VEVector4D( const VEREAL x, const VEREAL y, const VEREAL z, const VEREAL w );

/***
 * PURPOSE: Multiply vector with a number
 *  RETURN: Multiplied vector
 *   PARAM: [IN] v - vector to multiply
 *   PARAM: [IN] n - number
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR4D VEVector4DMult( const VEVECTOR4D v, const VEREAL n );

/***
 * PURPOSE: Determine vector projection to XYZ space
 *  RETURN: XYZ projetion
 *   PARAM: [IN] v - vector to project
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector4DXYZ( const VEVECTOR4D v );

/***
 * PURPOSE: Add two vectors
 *  RETURN: Sum of vectors
 *   PARAM: [IN] v1 - vector #1
 *   PARAM: [IN] v2 - vector #2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR4D VEVector4DAdd( const VEVECTOR4D v1, const VEVECTOR4D v2 );

/***
 * PURPOSE: Diff two vectors (v1 - v2)
 *  RETURN: Difference of vectors
 *   PARAM: [IN] v1 - vector #1
 *   PARAM: [IN] v2 - vector #2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR4D VEVector4DDiff( const VEVECTOR4D v1, const VEVECTOR4D v2 );

/***
 * PURPOSE: Mix two 4D vectors according to formula: alpha*v1 + (1-alpha)*v2
 *  RETURN: Mixed vector
 *   PARAM: [IN] v1    - first part
 *   PARAM: [IN] v2    - second part
 *   PARAM: [IN] alpha - mixing coefficient (alpha must be from [0; 1.0])
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR4D VEVector4DMix( const VEVECTOR4D v1, const VEVECTOR4D v2, const VEREAL alpha );

#endif // VECTOR4D_H_INCLUDED
