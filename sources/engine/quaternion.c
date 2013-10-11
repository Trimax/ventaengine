#include "quaternion.h"

#include <math.h>

/***
 * PURPOSE: Create a new quaternion
 *  RETURN: Created vector
 *   PARAM: [IN] v     - base vector
 *   PARAM: [IN] angle - rotation angle
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternion( const VEVECTOR3D v, const VEREAL angle )
{
  VEVECTOR3D vec = VEVector3DMult(v, sin(angle * 0.5));
  return VEVector4D(vec.m_X, vec.m_Y, vec.m_Z, cos(angle * 0.5));
} /* End of 'VEQuaternion' function */

/***
 * PURPOSE: Multiply quaternion with a number
 *  RETURN: Multiplied quaternion
 *   PARAM: [IN] v - quaternion to multiply
 *   PARAM: [IN] n - number
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternionMult( const VEQUATERNION v, const VEREAL n )
{
  return VEVector4DMult(v, n);
} /* End of '' function */

/***
 * PURPOSE: Add two quaternions
 *  RETURN: Sum of quaternions
 *   PARAM: [IN] q1 - quaternion #1
 *   PARAM: [IN] q2 - quaternion #2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternionAdd( const VEQUATERNION q1, const VEQUATERNION q2 )
{
  return VEVector4DAdd(q1, q2);
} /* End of 'VEQuaternionAdd' function */

/***
 * PURPOSE: Diff two quaternions (v1 - v2)
 *  RETURN: Difference of quaternions
 *   PARAM: [IN] q1 - quaternion #1
 *   PARAM: [IN] q2 - quaternion #2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternionDiff( const VEQUATERNION q1, const VEQUATERNION q2 )
{
  return VEVector4DDiff(q1, q2);
} /* End of 'VEQuaternionDiff' function */

/***
 * PURPOSE: Multiply two quaternions
 *  RETURN: Multiplication of quaternions
 *   PARAM: [IN] q1 - quaternion #1
 *   PARAM: [IN] q2 - quaternion #2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternionMultiply( const VEQUATERNION q1, const VEQUATERNION q2 )
{
  VEVECTOR3D v1 = VEVector4DXYZ(q1);
  VEVECTOR3D v2 = VEVector4DXYZ(q2);
  VEVECTOR3D v  = VEVector3DAdd(VEVector3DAdd(VEVector3DMult(v2, q1.m_W), VEVector3DMult(v1, q2.m_W)), VEVector3DCross(v1, v2));
  return VEVector4D(v.m_X, v.m_Y, v.m_Z, q1.m_W * q2.m_W - VEVector3DDot(v1, v2));
} /* End of 'VEQuaternionMultiply' function */

/***
 * PURPOSE: Determine quaternion norm
 *  RETURN: Quaternion's norm
 *   PARAM: [IN] v - quaternion to determine norm
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEQuaternionNorm( const VEQUATERNION q )
{
  return q.m_X * q.m_X + q.m_Y * q.m_Y + q.m_Z * q.m_Z + q.m_W * q.m_W;
} /* End of 'VEQuaternionNorm' function */

/***
 * PURPOSE: Determine quaternion's magnitude
 *  RETURN: Quaternion's magnitude
 *   PARAM: [IN] v - quaternion to determine magnitude
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEQuaternionMagnitude( const VEQUATERNION q )
{
  return pow(VEQuaternionNorm(q), 0.5);
} /* End of 'VEQuaternionMagnitude' function */

/***
 * PURPOSE: Determine quaternion's conjugate
 *  RETURN: Conjugated quaternion
 *   PARAM: [IN] v - quaternion to determine conjugate
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternionConjugate( const VEQUATERNION q )
{
  return VEVector4D(-q.m_X, -q.m_Y, -q.m_Z, q.m_W);
} /* End of 'VEQuaternionConjugate' function */

/***
 * PURPOSE: Determine inverse quaternion
 *  RETURN: Inversed quaternion
 *   PARAM: [IN] v - quaternion to inverse
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternionInverse( const VEQUATERNION q )
{
  return VEQuaternionMult(VEQuaternionConjugate(q), 1.0 / VEQuaternionNorm(q));
} /* End of 'VEQuaternionInverse' function */

/***
 * PURPOSE: Determine dot product of quaternions
 *  RETURN: Quaternions dot product
 *   PARAM: [IN] q1 - quaternion #1
 *   PARAM: [IN] q2 - quaternion #2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEQuaternionDot( const VEQUATERNION q1, const VEQUATERNION q2 )
{
  return q1.m_X * q2.m_X + q1.m_Y * q2.m_Y + q1.m_Z * q2.m_Z + q1.m_W * q2.m_W;
} /* End of 'VEQuaternionDot' function */

/***
 * PURPOSE: Rotate 3D vector using quaternion
 *  RETURN: Rotated 3D vector
 *   PARAM: [IN] v - vector to rotate
 *   PARAM: [IN] q - rotation quaternion
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DRotate( const VEVECTOR3D v, const VEQUATERNION q )
{
  VEQUATERNION vec = VEVector4D(v.m_X, v.m_Y, v.m_Z, 0.0);
  return VEVector4DXYZ(VEQuaternionMultiply(VEQuaternionMultiply(q, vec), VEQuaternionInverse(q)));
} /* End of 'VEVector3DRotate' function */

/***
 * PURPOSE: Converts quaternion to 3D rotation matrix
 *  RETURN: Rotation matrix
 *   PARAM: [IN] q - quaternion to convert
 *  AUTHOR: Eliseev Dmitry
 ***/
VEMATRIX3D VEQuaternionToMatrix( const VEQUATERNION q )
{
  VEREAL wx, wy, wz, xx, yy, yz, xy, xz, zz, x2, y2, z2;
  VEREAL s = 2.0f / VEQuaternionNorm(q);
  VEMATRIX3D matrix = VEMatrix3D(0.0);

  x2 = q.m_X * s;
  y2 = q.m_Y * s;
  z2 = q.m_Z * s;

  xx = q.m_X * x2;
  xy = q.m_X * y2;
  xz = q.m_X * z2;

  yy = q.m_Y * y2;
  yz = q.m_Y * z2;
  zz = q.m_Y * z2;

  wx = q.m_W * x2;
  wy = q.m_W * y2;
  wz = q.m_W * z2;

  /* 1st row */
  matrix.m_Items[0][0] = 1.0f - (yy + zz);
  matrix.m_Items[1][0] = xy - wz;
  matrix.m_Items[2][0] = xz + wy;

  /* 2nd row */
  matrix.m_Items[0][1] = xy + wz;
  matrix.m_Items[1][1] = 1.0f - (xx + zz);
  matrix.m_Items[2][1] = yz - wx;

  /* 3rd row */
  matrix.m_Items[0][2] = xz - wy;
  matrix.m_Items[1][2] = yz + wx;
  matrix.m_Items[2][2] = 1.0f - (xx + yy);

  /* That's it */
  return matrix;
} /* End of 'VEQuaternionToMatrix' function */

/***
 * PURPOSE: Determine interpolated quaternion using SLERP
 *  RETURN: Interpolated quaternion
 *   PARAM: [IN] q1 - startup quaternion
 *   PARAM: [IN] q1 - finish quaternion
 *   PARAM: [IN] t  - interpolation parameter (must be from [0; 1])
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternionInterpolate( const VEQUATERNION q1, const VEQUATERNION q2, const VEREAL t )
{
  VEQUATERNION i1 = q1, i2 = q2, r;
  VEREAL cosOmega = VEQuaternionDot(q1, q2), sinOmega = pow(1.0 - cosOmega * cosOmega, 0.5);
  VEREAL omega = 0.0;

  /* Check t bound */
  if (t <= 0.0)
    return q1;
  if (t >= 1.0)
    return q2;

  /* Limit lower bound */
  if (cosOmega < 0.0)
  {
    cosOmega = -cosOmega;
    i2 = VEVector4D(-q2.m_X, -q2.m_Y, -q2.m_Z, -q2.m_W);
  }

  /* Limit upper bound */
  if (cosOmega > 0.9999)
    cosOmega = 0.9999;

  /* Determine omega angle */
  omega = acos(cosOmega);

  { /* Mix quaternions */
    VEREAL k1 = sin((1.0-t)*omega);
    VEREAL k2 = sin(t*omega);

    r.m_X = i1.m_X * k1 + i2.m_X * k2;
    r.m_Y = i1.m_Y * k1 + i2.m_Y * k2;
    r.m_Z = i1.m_Z * k1 + i2.m_Z * k2;
    r.m_W = i1.m_W * k1 + i2.m_W * k2;
  }

  /* That's it */
  return VEVector4DMult(r, 1.0 / sinOmega);
} /* End of 'VEQuaternionInterpolate' function */
