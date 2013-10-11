#ifndef SECTION_H_INCLUDED
#define SECTION_H_INCLUDED

#ifdef _WIN32
#include <windows.h>
#else
#include <pthread.h>
#endif

#include "types.h"

/* Critical section structure definition */
typedef struct tagVECRITICALSECTION
{
  #ifdef _WIN32
  CRITICAL_SECTION m_Section;
  #else
  pthread_mutex_t m_Mutex;
  #endif

  VEBOOL m_IsUsed;     /* Is section in use */
} VECRITICALSECTION;

/***
 * PURPOSE: Initialize critical sections manager
 *   PARAM: [IN] maximalSectionsNumber - maximal allowed critical sections
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VESectionInit( const VEUINT maximalSectionsNumber );

/***
 * PURPOSE: Deinitialize critical sections manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionDeinit( VEVOID );

/***
 * PURPOSE: Wait while critical system section is busy and after enter in it
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionSystemEnter( VEVOID );

/***
 * PURPOSE: Leave system critical section
 *   PARAM: [IN] sectionID - critical section identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionSystemLeave( VEVOID );

/*** Internal functions for synchronizing objects ***/

/***
 * PURPOSE: Create a critical section for internal use only
 *  RETURN: Created critical section
 *   PARAM: [IN] comment - internal critical section comment
 *  AUTHOR: Eliseev Dmitry
 ***/
VECRITICALSECTION *VESectionCreateInternal( VEBUFFER comment );

/***
 * PURPOSE: Delete an internal critical section
 *   PARAM: [IN] sectionPtr - pointer to created critical section
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionDeleteInternal( VECRITICALSECTION *sectionPtr );

/***
 * PURPOSE: Enter in internal critical section
 *   PARAM: [IN] sectionPtr - pointer to created critical section
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionEnterInternal( const VECRITICALSECTION *sectionPtr );

/***
 * PURPOSE: Leave an internal critical section
 *   PARAM: [IN] sectionPtr - pointer to created critical section
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionLeaveInternal( const VECRITICALSECTION *sectionPtr );

#endif // SECTION_H_INCLUDED
