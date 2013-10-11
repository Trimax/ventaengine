#include "tests.h"

#include <math.h>

/***
 * PURPOSE: Print's quaternion
 *   PARAM: [IN] q - quaternion to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID QuaternionPrint( const VEQUATERNION q )
{
  printf("[%.2lf, %.2lf, %.2lf, %.2lf]\n", q.m_X, q.m_Y, q.m_Z, q.m_W);
} /* End of 'QuaternionPrint' function */

/***
 * PURPOSE: Print's 3D matrix
 *   PARAM: [IN] m - 3D matrix to print
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID Matrix3DPrint( const VEMATRIX3D m )
{
  printf("[%.2lf, %.2lf, %.2lf]\n", m.m_Items[0][0], m.m_Items[0][1], m.m_Items[0][2]);
  printf("[%.2lf, %.2lf, %.2lf]\n", m.m_Items[1][0], m.m_Items[1][1], m.m_Items[1][2]);
  printf("[%.2lf, %.2lf, %.2lf]\n", m.m_Items[2][0], m.m_Items[2][1], m.m_Items[2][2]);
} /* End of 'Matrix3DPrint' function */

/***
 * PURPOSE: Math test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestMath( VEVOID )
{
  VEVECTOR3D   i  = VEVector3D(1.0, 0.0, 0.0);
  VEVECTOR3D   j  = VEVector3D(0.0, 1.0, 0.0);
  VEVECTOR3D   k  = VEVector3D(0.0, 0.0, 1.0);
  VEQUATERNION q1 = VEQuaternion(i, M_PI_2);
  VEQUATERNION q2 = VEQuaternion(j, M_PI_2);
  VEQUATERNION q3 = VEQuaternion(k, M_PI_2);

  VEVECTOR3D   v  = VEVector3D(0.1, 0.3, 0.6);
  VEQUATERNION q  = VEQuaternion(v, M_PI_2);

  printf("i = [%.2lf, %.2lf, %.2lf]\n", i.m_X, i.m_Y, i.m_Z);
  printf("j = [%.2lf, %.2lf, %.2lf]\n", j.m_X, j.m_Y, j.m_Z);
  printf("k = [%.2lf, %.2lf, %.2lf]\n", k.m_X, k.m_Y, k.m_Z);
  printf("v = [%.2lf, %.2lf, %.2lf]\n", v.m_X, v.m_Y, v.m_Z);
  printf("\n");

  printf("   q1 = "); QuaternionPrint(q1);
  printf("   q2 = "); QuaternionPrint(q2);
  printf("   q3 = "); QuaternionPrint(q3);
  printf("    q = "); QuaternionPrint(q);
  printf("\n");

  { /* Vectors multiplication */
    VEVECTOR3D cv  = VEVector3DCross(i, j);
    VEREAL dotProd = VEVector3DDot(i, j);
    printf("  ixj = [%.2lf, %.2lf, %.2lf]\n", cv.m_X, cv.m_Y, cv.m_Z);
    printf("   ij = %.2lf\n", dotProd);
    printf("\n");
  }

  { /* Test quaternion sum & difference */
    VEQUATERNION sum   = VEQuaternionAdd(q1, q2);
    VEQUATERNION diff  = VEQuaternionDiff(q1, q2);
    printf("q1+q2 = "); QuaternionPrint(sum);
    printf("q1-q2 = "); QuaternionPrint(diff);
    printf("\n");
  }

  { /* Test quaternion multiplication */
    VEQUATERNION t = VEQuaternionMult(q, 2.0);
    printf("q*2.0 = "); QuaternionPrint(t);
    printf("\n");
  }

  { /* Test quaternion multiplication */
    VEQUATERNION m1 = VEQuaternionMultiply(q1, q2);
    VEQUATERNION m2 = VEQuaternionMultiply(q2, q1);
    printf("q1*q2 = "); QuaternionPrint(m1);
    printf("q2*q1 = "); QuaternionPrint(m2);
    printf("\n");
  }

  { /* Test dot, norm, magnitude */
    VEREAL dotProduct = VEQuaternionDot(q1, q2);
    VEREAL norm       = VEQuaternionNorm(q);
    VEREAL magnitude  = VEQuaternionMagnitude(q);

    printf(" q1q2 = %.2lf\n", dotProduct);
    printf(" N(q) = %.2lf\n", norm);
    printf("  |q| = %.2lf\n", magnitude);
    printf("\n");
  }

  { /* Test conjugate & inversion */
    VEQUATERNION cq = VEQuaternionConjugate(q);
    VEQUATERNION iq = VEQuaternionInverse(q);
    VEQUATERNION m1 = VEQuaternionMultiply(q, iq);
    VEQUATERNION m2 = VEQuaternionMultiply(iq, q);

    printf(" c(q) = "); QuaternionPrint(cq);
    printf(" q^-1 = "); QuaternionPrint(iq);
    printf(" q*iq = "); QuaternionPrint(m1);
    printf(" iq*q = "); QuaternionPrint(m2);
    printf("\n");
  }

  { /* Vectors rotation */
    VEVECTOR3D r1 = VEVector3DRotate(i, q2);
    VEVECTOR3D r2 = VEVector3DRotate(k, q2);
    VEVECTOR3D r3 = VEVector3DRotate(j, q1);

    printf("(i,j) = [%.2lf, %.2lf, %.2lf]\n", r1.m_X, r1.m_Y, r1.m_Z);
    printf("(k,j) = [%.2lf, %.2lf, %.2lf]\n", r2.m_X, r2.m_Y, r2.m_Z);
    printf("(j,i) = [%.2lf, %.2lf, %.2lf]\n", r3.m_X, r3.m_Y, r3.m_Z);
    printf("\n");
  }

  { /* SLERP */
    VEQUATERNION t1 = VEQuaternionInterpolate(q2, q1, 0.0);
    VEQUATERNION t2 = VEQuaternionInterpolate(q2, q1, 0.5);
    VEQUATERNION t3 = VEQuaternionInterpolate(q2, q1, 1.0);

    printf("   t1 = "); QuaternionPrint(t1);
    printf("   t2 = "); QuaternionPrint(t2);
    printf("   t3 = "); QuaternionPrint(t3);
    printf("\n");
  }

  { /* Test quaternion to matrix conversion */
    VEMATRIX3D m = VEQuaternionToMatrix(q1);
    printf("M(q1):\n");
    Matrix3DPrint(m);
    printf("\n");
  }
} /* End of 'TestMath' function */
