#include "matrix3d.h"

#include <string.h>

/***
 * PURPOSE: Create a new 3D matrix and fill it with number
 *  RETURN: Created matrix
 *   PARAM: [IN] n - number to fill matrix
 *  AUTHOR: Eliseev Dmitry
 ***/
VEMATRIX3D VEMatrix3D( const VEREAL n )
{
  VEMATRIX3D matrix;
  memset(&matrix, 0, sizeof(VEMATRIX3D));

  /* Initialize 1st row */
  matrix.m_Items[0][0] = n;
  matrix.m_Items[0][1] = n;
  matrix.m_Items[0][2] = n;

  /* Initialize 2nd row */
  matrix.m_Items[1][0] = n;
  matrix.m_Items[1][1] = n;
  matrix.m_Items[1][2] = n;

  /* Initialize 3rd row */
  matrix.m_Items[2][0] = n;
  matrix.m_Items[2][1] = n;
  matrix.m_Items[2][2] = n;

  /* That's it */
  return matrix;
} /* End of 'VEMatrix3D' function */
