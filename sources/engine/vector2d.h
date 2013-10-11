#ifndef VECTOR2D_H_INCLUDED
#define VECTOR2D_H_INCLUDED

#include "types.h"

/* 2D vector */
typedef struct tagVEVECTOR2D
{
  VEREAL m_X; /* Absciss */
  VEREAL m_Y; /* Ordinate */
} VEVECTOR2D;

/***
 * PURPOSE: Create a new vector by components
 *  RETURN: Created vector
 *   PARAM: [IN] x - absciss
 *   PARAM: [IN] y - ordinate
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR2D VEVector2D( const VEREAL x, const VEREAL y );

/***
 * PURPOSE: Add two 2D vectors
 *  RETURN: Summ vector
 *   PARAM: [IN] v1 - first part
 *   PARAM: [IN] v2 - second part
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR2D VEVector2DAdd( const VEVECTOR2D v1, const VEVECTOR2D v2 );

/***
 * PURPOSE: Diff two 2D vectors
 *  RETURN: Summ vector
 *   PARAM: [IN] v1 - first part
 *   PARAM: [IN] v2 - second part
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR2D VEVector2DSub( const VEVECTOR2D v1, const VEVECTOR2D v2 );

/***
 * PURPOSE: Multiply vector with a number
 *  RETURN: Summ vector
 *   PARAM: [IN] v - vector to multiply
 *   PARAM: [IN] n - number
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR2D VEVector2DMult( const VEVECTOR2D v, const VEREAL n );

/***
 * PURPOSE: Get an average between two vectors
 *  RETURN: Average vector
 *   PARAM: [IN] v1 - first vector
 *   PARAM: [IN] v2 - second second
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR2D VEVector2DAverage( const VEVECTOR2D v1, const VEVECTOR2D v2 );

#endif // VECTOR2D_H_INCLUDED
