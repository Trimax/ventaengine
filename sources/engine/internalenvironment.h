#ifndef INTERNALENVIRONMENT_H_INCLUDED
#define INTERNALENVIRONMENT_H_INCLUDED

#include "internalobject.h"
#include "internalcamera.h"
#include "internalsun.h"
#include "internalfog.h"
#include "types.h"

/* Scene type definition */
typedef struct tagVEENVIRONMENT
{
  VEOBJECT *m_Sky;      /* Background sphere */
  VECOLOR   m_SkyColor; /* Sky color */
  VESUN     m_Sun;      /* Sun object */
  VEFOG     m_Fog;      /* Fog parameters */
} VEENVIRONMENT;

/***
 * PURPOSE: Render sky spheres
 *   PARAM: [IN] env    - scene environment pointer
 *   PARAM: [IN] camera - current camera pointer
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEEnvironmentRenderInternal( VEENVIRONMENT *env, VECAMERA *camera );

/***
 * PURPOSE: Update environment
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEEnvironmentUpdateInternal( VEVOID );

#endif // INTERNALENVIRONMENT_H_INCLUDED
