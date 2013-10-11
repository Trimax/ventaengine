#ifndef INTERNALBOUNDSPHERE_H_INCLUDED
#define INTERNALBOUNDSPHERE_H_INCLUDED

#include "types.h"
#include "math.h"

/* Bound sphere */
typedef struct tagVEBOUNDINGSPHERE
{
  VEVECTOR3D m_Position; /* Bounding sphere position */
  VEREAL     m_Radius;   /* Bounding sphere radius */
} VEBOUNDINGSPHERE;

#endif // INTERNALBOUNDSPHERE_H_INCLUDED
