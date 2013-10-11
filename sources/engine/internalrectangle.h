#ifndef INTERNALRECTANGLE_H_INCLUDED
#define INTERNALRECTANGLE_H_INCLUDED

#include "math.h"

/* Rectangle definition */
typedef struct tagVERECTANGLE
{
  VEVECTOR2D m_Position;  /* Left-top corner position */
  VEVECTOR2D m_Size;      /* Rectangle size */
} VERECTANGLE;

#endif // INTERNALRECTANGLE_H_INCLUDED
