#ifndef INTERNALSUN_H_INCLUDED
#define INTERNALSUN_H_INCLUDED

#include "physics.h"
#include "math.h"

/* Scene type definition */
typedef struct tagVESUN
{
  VECOLOR    m_Color;     /* Sun color */
  VEVECTOR3D m_Position;  /* Real sun position */
  VEREAL     m_Velocity;  /* Sun velocity along orbit */
  VEREAL     m_Deviation; /* Sun deviation from 0-th meridian */
} VESUN;

#endif // INTERNALSUN_H_INCLUDED
