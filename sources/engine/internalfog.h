#ifndef INTERNALFOG_H_INCLUDED
#define INTERNALFOG_H_INCLUDED

#include "types.h"

/* Fog structure */
typedef struct tagVEFOG
{
  VEREAL     m_Density; /* Fog density */
  VEVECTOR4D m_Color;   /* Fog color */
} VEFOG;

#endif // INTERNALFOG_H_INCLUDED
