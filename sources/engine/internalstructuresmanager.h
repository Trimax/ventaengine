#ifndef INTERNALSTRUCTURESMANAGER_H_INCLUDED
#define INTERNALSTRUCTURESMANAGER_H_INCLUDED

#include "types.h"
#include "internalcriticalsection.h"

/* Default container size */
#define VE_DEFAULT_STRUCTURES 1024

/* Data structure */
typedef struct tagVESTRUCTURE
{
  VEPOINTER          m_Structure; /* Pointer to structure */
  VEUINT             m_Size;      /* Number of items */
  VEUINT             m_LastID;    /* Number of last added identifier */
  VEBYTE             m_Type;      /* Structure type */
  VECRITICALSECTION *m_Sync;      /* Critical section */
} VESTRUCTURE;

/***
 * PURPOSE: Initialize structures manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEStructureInit( VEVOID );

/***
 * PURPOSE: Deinitialize structures manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStructureDeinit( VEVOID );

#endif // INTERNALSTRUCTURESMANAGER_H_INCLUDED
