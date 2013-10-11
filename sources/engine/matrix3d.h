#ifndef MATRIX3D_H_INCLUDED
#define MATRIX3D_H_INCLUDED

#include "types.h"

/* 3D matrix */
typedef struct tagVEMATRIX3D
{
  VEREAL m_Items[3][3]; /* Matrix elements */
} VEMATRIX3D;

/***
 * PURPOSE: Create a new 3D matrix and fill it with number
 *  RETURN: Created matrix
 *   PARAM: [IN] n - number to fill matrix
 *  AUTHOR: Eliseev Dmitry
 ***/
VEMATRIX3D VEMatrix3D( const VEREAL n );

#endif // MATRIX3D_H_INCLUDED
