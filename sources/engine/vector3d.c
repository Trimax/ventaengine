#include "vector3d.h"

#include <math.h>

/***
 * PURPOSE: Create a new vector by components
 *  RETURN: Created vector
 *   PARAM: [IN] x - absciss
 *   PARAM: [IN] y - ordinate
 *   PARAM: [IN] z - applicate
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3D( const VEREAL x, const VEREAL y, const VEREAL z )
{
  VEVECTOR3D vec = {x, y, z};
  return vec;
} /* End of 'VEVector3D' function */

/***
 * PURPOSE: Add two 3D vectors
 *  RETURN: Summ vector
 *   PARAM: [IN] v1 - first part
 *   PARAM: [IN] v2 - second part
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DAdd( const VEVECTOR3D v1, const VEVECTOR3D v2 )
{
  return VEVector3D(v1.m_X + v2.m_X, v1.m_Y + v2.m_Y, v1.m_Z + v2.m_Z);
} /* End of 'VEVector3DAdd' function */

/***
 * PURPOSE: Diff two 3D vectors
 *  RETURN: Summ vector
 *   PARAM: [IN] v1 - first part
 *   PARAM: [IN] v2 - second part
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DSub( const VEVECTOR3D v1, const VEVECTOR3D v2 )
{
  return VEVector3D(v1.m_X - v2.m_X, v1.m_Y - v2.m_Y, v1.m_Z - v2.m_Z);
} /* End of 'VEVector3DSub' function */

/***
 * PURPOSE: Multiply vector with a number
 *  RETURN: Multiplied vector
 *   PARAM: [IN] v - vector to multiply
 *   PARAM: [IN] n - number
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DMult( const VEVECTOR3D v, const VEREAL n )
{
  return VEVector3D(v.m_X * n, v.m_Y * n, v.m_Z * n);
} /* End of 'VEVector3DMult' function */

/***
 * PURPOSE: Determine vector norm
 *  RETURN: Vector's norm
 *   PARAM: [IN] v - vector to determine norm
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEVector3DNorm( const VEVECTOR3D v )
{
  return pow(v.m_X * v.m_X + v.m_Y * v.m_Y + v.m_Z * v.m_Z, 0.5);
} /* End of 'VEVector3DNorm' function */

/***
 * PURPOSE: Cross product of two vectors
 *  RETURN: Cross product of two vectors
 *   PARAM: [IN] v1 - left vector
 *   PARAM: [IN] v2 - right vector
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DCross( const VEVECTOR3D v1, const VEVECTOR3D v2 )
{
  return VEVector3D(v1.m_Y * v2.m_Z - v1.m_Z * v2.m_Y,
                    v1.m_Z * v2.m_X - v1.m_X * v2.m_Z,
                    v1.m_X * v2.m_Y - v1.m_Y * v2.m_X);
} /* End of 'VEVector3DCross' function */

/***
 * PURPOSE: Dot product of two vectors
 *  RETURN: Dot product of two vectors
 *   PARAM: [IN] v1 - left vector
 *   PARAM: [IN] v2 - right vector
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEVector3DDot( const VEVECTOR3D v1, const VEVECTOR3D v2 )
{
  return v1.m_X * v2.m_X + v1.m_Y * v2.m_Y + v1.m_Z * v2.m_Z;
} /* End of 'VEVector3DDot' function */

/***
 * PURPOSE: Normalize vector
 *  RETURN: Normalized vector
 *   PARAM: [IN] v - vector to normalize
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DNormalize( const VEVECTOR3D v )
{
  VEREAL inversedNorm = 1.0 / VEVector3DNorm(v);
  return VEVector3D(v.m_X * inversedNorm, v.m_Y * inversedNorm, v.m_Z * inversedNorm);
} /* End of 'VEVector3DNormalize' function */

/***
 * PURPOSE: Euclid distance square between two points
 *  RETURN: Euclid distance square
 *   PARAM: [IN] v1 - point 1
 *   PARAM: [IN] v2 - point 2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEVector3DDistanceSquare( const VEVECTOR3D v1, const VEVECTOR3D v2 )
{
  return pow(v1.m_X - v2.m_X, 2.0) + pow(v1.m_Y - v2.m_Y, 2.0) + pow(v1.m_Z - v2.m_Z, 2.0);
} /* End of 'VEVector3DDistanceSquare' function */

/***
 * PURPOSE: Euclid distance between two points
 *  RETURN: Euclid distance
 *   PARAM: [IN] v1 - point 1
 *   PARAM: [IN] v2 - point 2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEVector3DDistance( const VEVECTOR3D v1, const VEVECTOR3D v2 )
{
  return pow(VEVector3DDistanceSquare(v1, v2), 0.5);
} /* End of 'VEVector3DDistance' function */

/***
 * PURPOSE: Mix two 3D vectors according to formula: alpha*v1 + (1-alpha)*v2
 *  RETURN: Mixed vector
 *   PARAM: [IN] v1    - first part
 *   PARAM: [IN] v2    - second part
 *   PARAM: [IN] alpha - mixing coefficient (alpha must be from [0; 1.0])
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DMix( const VEVECTOR3D v1, const VEVECTOR3D v2, const VEREAL alpha )
{
  return VEVector3D(v1.m_X * alpha + v2.m_X * (1.0 - alpha),
                    v1.m_Y * alpha + v2.m_Y * (1.0 - alpha),
                    v1.m_Z * alpha + v2.m_Z * (1.0 - alpha));
} /* End of 'VEVector3DMix' function */
