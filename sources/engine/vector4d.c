#include "vector4d.h"

/***
 * PURPOSE: Create a new vector by components
 *  RETURN: Created vector
 *   PARAM: [IN] x - absciss
 *   PARAM: [IN] y - ordinate
 *   PARAM: [IN] z - applicate
 *   PARAM: [IN] w - additional
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR4D VEVector4D( const VEREAL x, const VEREAL y, const VEREAL z, const VEREAL w )
{
  VEVECTOR4D vec = {x, y, z, w};
  return vec;
} /* End of 'VEVector4D' function */

/***
 * PURPOSE: Determine vector projection to XYZ space
 *  RETURN: XYZ projetion
 *   PARAM: [IN] v - vector to project
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector4DXYZ( const VEVECTOR4D v )
{
  VEVECTOR3D vec = {v.m_X, v.m_Y, v.m_Z};
  return vec;
} /* End of 'VEVector4DXYZ' function */

/***
 * PURPOSE: Multiply vector with a number
 *  RETURN: Multiplied vector
 *   PARAM: [IN] v - vector to multiply
 *   PARAM: [IN] n - number
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR4D VEVector4DMult( const VEVECTOR4D v, const VEREAL n )
{
  return VEVector4D(v.m_X * n, v.m_Y * n, v.m_Z * n, v.m_W * n);
} /* End of 'VEVector4DMult' function */

/***
 * PURPOSE: Add two vectors
 *  RETURN: Sum of vectors
 *   PARAM: [IN] v1 - vector #1
 *   PARAM: [IN] v2 - vector #2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR4D VEVector4DAdd( const VEVECTOR4D v1, const VEVECTOR4D v2 )
{
  return VEVector4D(v1.m_X + v2.m_X, v1.m_Y + v2.m_Y, v1.m_Z + v2.m_Z, v1.m_W + v2.m_W);
} /* End of 'VEVector4DAdd' function */

/***
 * PURPOSE: Diff two vectors (v1 - v2)
 *  RETURN: Difference of vectors
 *   PARAM: [IN] v1 - vector #1
 *   PARAM: [IN] v2 - vector #2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR4D VEVector4DDiff( const VEVECTOR4D v1, const VEVECTOR4D v2 )
{
  return VEVector4D(v1.m_X - v2.m_X, v1.m_Y - v2.m_Y, v1.m_Z - v2.m_Z, v1.m_W - v2.m_W);
} /* End of 'VEVector4DDiff' function */

/***
 * PURPOSE: Mix two 4D vectors according to formula: alpha*v1 + (1-alpha)*v2
 *  RETURN: Mixed vector
 *   PARAM: [IN] v1    - first part
 *   PARAM: [IN] v2    - second part
 *   PARAM: [IN] alpha - mixing coefficient (alpha must be from [0; 1.0])
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR4D VEVector4DMix( const VEVECTOR4D v1, const VEVECTOR4D v2, const VEREAL alpha )
{
  return VEVector4D(v1.m_X * alpha + v2.m_X * (1.0 - alpha),
                    v1.m_Y * alpha + v2.m_Y * (1.0 - alpha),
                    v1.m_Z * alpha + v2.m_Z * (1.0 - alpha),
                    v1.m_W * alpha + v2.m_W * (1.0 - alpha));
} /* End of 'VEVector4DMix' function */
