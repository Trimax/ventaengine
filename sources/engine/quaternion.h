#ifndef QUATERNION_H_INCLUDED
#define QUATERNION_H_INCLUDED

#include "vector4d.h"
#include "matrix3d.h"

/* Define quaternion structure */
typedef VEVECTOR4D VEQUATERNION;

/***
 * PURPOSE: Create a new quaternion
 *  RETURN: Created vector
 *   PARAM: [IN] v     - base vector
 *   PARAM: [IN] angle - rotation angle
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternion( const VEVECTOR3D v, const VEREAL angle );

/***
 * PURPOSE: Multiply quaternion with a number
 *  RETURN: Multiplied quaternion
 *   PARAM: [IN] v - quaternion to multiply
 *   PARAM: [IN] n - number
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternionMult( const VEQUATERNION v, const VEREAL n );

/***
 * PURPOSE: Add two quaternions
 *  RETURN: Sum of quaternions
 *   PARAM: [IN] q1 - quaternion #1
 *   PARAM: [IN] q2 - quaternion #2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternionAdd( const VEQUATERNION q1, const VEQUATERNION q2 );

/***
 * PURPOSE: Diff two quaternions (q1 - q2)
 *  RETURN: Difference of quaternions
 *   PARAM: [IN] q1 - quaternion #1
 *   PARAM: [IN] q2 - quaternion #2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternionDiff( const VEQUATERNION q1, const VEQUATERNION q2 );

/***
 * PURPOSE: Multiply two quaternions
 *  RETURN: Multiplication of quaternions
 *   PARAM: [IN] q1 - quaternion #1
 *   PARAM: [IN] q2 - quaternion #2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternionMultiply( const VEQUATERNION q1, const VEQUATERNION q2 );

/***
 * PURPOSE: Determine dot product of quaternions
 *  RETURN: Quaternions dot product
 *   PARAM: [IN] q1 - quaternion #1
 *   PARAM: [IN] q2 - quaternion #2
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEQuaternionDot( const VEQUATERNION q1, const VEQUATERNION q2 );

/***
 * PURPOSE: Determine quaternion's norm
 *  RETURN: Quaternion's norm
 *   PARAM: [IN] v - quaternion to determine norm
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEQuaternionNorm( const VEQUATERNION q );

/***
 * PURPOSE: Determine quaternion's magnitude
 *  RETURN: Quaternion's magnitude
 *   PARAM: [IN] v - quaternion to determine magnitude
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEQuaternionMagnitude( const VEQUATERNION q );

/***
 * PURPOSE: Determine quaternion's conjugate
 *  RETURN: Conjugated quaternion
 *   PARAM: [IN] v - quaternion to determine conjugate
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternionConjugate( const VEQUATERNION q );

/***
 * PURPOSE: Determine inverse quaternion
 *  RETURN: Inversed quaternion
 *   PARAM: [IN] v - quaternion to inverse
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternionInverse( const VEQUATERNION q );

/***
 * PURPOSE: Determine interpolated quaternion using SLERP
 *  RETURN: Interpolated quaternion
 *   PARAM: [IN] q1 - startup quaternion
 *   PARAM: [IN] q1 - finish quaternion
 *   PARAM: [IN] t  - interpolation parameter (must be from [0; 1])
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUATERNION VEQuaternionInterpolate( const VEQUATERNION q1, const VEQUATERNION q2, const VEREAL t );

/***
 * PURPOSE: Converts quaternion to 3D rotation matrix
 *  RETURN: Rotation matrix
 *   PARAM: [IN] q - quaternion to convert
 *  AUTHOR: Eliseev Dmitry
 ***/
VEMATRIX3D VEQuaternionToMatrix( const VEQUATERNION q );

/***
 * PURPOSE: Rotate 3D vector using quaternion
 *  RETURN: Rotated 3D vector
 *   PARAM: [IN] v - vector to rotate
 *   PARAM: [IN] q - rotation quaternion
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVECTOR3D VEVector3DRotate( const VEVECTOR3D v, const VEQUATERNION q );

#endif // QUATERNION_H_INCLUDED
