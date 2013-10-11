#include "vector2d.h"

/***
 * PURPOSE: Create a new vector by components
 *  RETURN: Created vector
 *   PARAM: [IN] x - absciss
 *   PARAM: [IN] y - ordinate
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR2D VEVector2D( const VEREAL x, const VEREAL y )
{
  VEVECTOR2D vec = {x, y};
  return vec;
} /* End of 'VEVector2D' function */

/***
 * PURPOSE: Add two 2D vectors
 *  RETURN: Summ vector
 *   PARAM: [IN] v1 - first part
 *   PARAM: [IN] v2 - second part
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR2D VEVector2DAdd( const VEVECTOR2D v1, const VEVECTOR2D v2 )
{
  return VEVector2D(v1.m_X + v2.m_X, v1.m_Y + v2.m_Y);
} /* End of 'VEVector2DAdd' function */

/***
 * PURPOSE: Diff two 2D vectors
 *  RETURN: Summ vector
 *   PARAM: [IN] v1 - first part
 *   PARAM: [IN] v2 - second part
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR2D VEVector2DSub( const VEVECTOR2D v1, const VEVECTOR2D v2 )
{
  return VEVector2D(v1.m_X - v2.m_X, v1.m_Y - v2.m_Y);
} /* End of 'VEVector2DSub' function */

/***
 * PURPOSE: Multiply vector with a number
 *  RETURN: Summ vector
 *   PARAM: [IN] v - vector to multiply
 *   PARAM: [IN] n - number
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR2D VEVector2DMult( const VEVECTOR2D v, const VEREAL n )
{
  return VEVector2D(v.m_X * n, v.m_Y * n);
} /* End of 'VEVector2DMult' function */

/***
 * PURPOSE: Get an average between two vectors
 *  RETURN: Average vector
 *   PARAM: [IN] v1 - first vector
 *   PARAM: [IN] v2 - second second
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR2D VEVector2DAverage( const VEVECTOR2D v1, const VEVECTOR2D v2 )
{
  return VEVector2D(0.5 * (v1.m_X + v2.m_X), 0.5 * (v1.m_Y + v2.m_Y));
} /* End of 'VEVector2DAverage' function */
