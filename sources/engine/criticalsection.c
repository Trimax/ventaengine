#include "criticalsection.h"
#include "memorymanager.h"
#include "internalcriticalsection.h"

#include <stdlib.h>
#include <string.h>

/* Maximal number of critical sections */
volatile static VEUINT p_SectionsMaximalNumber = 0;

/* Critical sections manager initialization flag */
volatile static VEBOOL p_SectionsManagerInitialized = FALSE;

/* Critical sections array */
volatile static VECRITICALSECTION *p_Sections = NULL;

/* Internal engine critical section */
volatile static VECRITICALSECTION p_SectionEngine;

/***
 * PURPOSE: Initialize critical sections manager
 *   PARAM: [IN] configuration - pointer to filled configuration
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VESectionInit( const VEUINT maximalSectionsNumber )
{
  VEUINT sectionID = 0;

  /* Initialize system section */
  memset((VEPOINTER)&p_SectionEngine, 0, sizeof(VECRITICALSECTION));

  #ifdef _WIN32
  InitializeCriticalSection((CRITICAL_SECTION*)&p_SectionEngine.m_Section);
  #else
  memset((VEPOINTER)&p_SectionEngine.m_Mutex, 0, sizeof(pthread_mutex_t));
  #endif

  /* Check arguments */
  if (maximalSectionsNumber == 0)
  {
    p_SectionsManagerInitialized = TRUE;
    return TRUE;
  }

  /* Allocate critical sections array */
  p_Sections = malloc(sizeof(VECRITICALSECTION) * maximalSectionsNumber);
  if (!p_Sections)
  {
    VESectionDeinit();
    return FALSE;
  }

  /* Store maximal sections & threads number */
  p_SectionsMaximalNumber = maximalSectionsNumber;

  /* Prepare each critical section */
  memset((VEPOINTER)p_Sections, 0, sizeof(VECRITICALSECTION) * maximalSectionsNumber);
  for (sectionID = 0; sectionID < maximalSectionsNumber; sectionID++)
  {
    #ifdef _WIN32
    InitializeCriticalSection((CRITICAL_SECTION*)&p_Sections[sectionID].m_Section);
    #else
    memset((VEPOINTER)&p_Sections[sectionID].m_Mutex, 0, sizeof(pthread_mutex_t));
    #endif
  }

  /* Sections manager initialized */
  p_SectionsManagerInitialized = TRUE;
  return TRUE;
} /* End of 'VESectionInit' function */

/***
 * PURPOSE: Deinitialize critical sections manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionDeinit( VEVOID )
{
  #ifdef _WIN32
  DeleteCriticalSection((CRITICAL_SECTION*)&p_SectionEngine.m_Section);
  #endif

  /* Reset system critical section */
  memset((VEPOINTER)&p_SectionEngine, 0, sizeof(VECRITICALSECTION));

  /* Remove critical sections */
  if (p_Sections)
  {
    VEUINT sectionID = 0;
    for (sectionID = 0; sectionID < p_SectionsMaximalNumber; sectionID++)
    {
      #ifdef _WIN32
      DeleteCriticalSection((CRITICAL_SECTION*)&p_Sections[sectionID].m_Section);
      #endif
    }

    /* Remove sections array */
    free((VEPOINTER)p_Sections);
    p_Sections = NULL;
  }

  /* Deinitialization finished */
  p_SectionsManagerInitialized = FALSE;
} /* End of 'VESectionDeinit' function */

/***
 * PURPOSE: Wait while critical system section is busy and after enter in it
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionSystemEnter( VEVOID )
{
  if (!p_SectionsManagerInitialized)
    return;

  #ifdef _WIN32
  EnterCriticalSection((CRITICAL_SECTION*)&p_SectionEngine.m_Section);
  #else
  pthread_mutex_lock((pthread_mutex_t*)&p_SectionEngine.m_Mutex);
  #endif
} /* End of 'VESectionSystemEnter' function */

/***
 * PURPOSE: Leave system critical section
 *   PARAM: [IN] sectionID - critical section identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionSystemLeave( VEVOID )
{
  #ifdef _WIN32
  LeaveCriticalSection((CRITICAL_SECTION*)&p_SectionEngine.m_Section);
  #else
  pthread_mutex_unlock((pthread_mutex_t*)&p_SectionEngine.m_Mutex);
  #endif
} /* End of 'VESectionSystemLeave' function */

/***
 * PURPOSE: Wait while critical section is busy and after enter in it
 *   PARAM: [IN] sectionID - critical section identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionEnter( const VEUINT sectionID )
{
  if ((sectionID > 0)&&(sectionID < p_SectionsMaximalNumber))
  {
    #ifdef _WIN32
    EnterCriticalSection((CRITICAL_SECTION*)&p_Sections[sectionID].m_Section);
    #else
    pthread_mutex_lock((pthread_mutex_t*)&p_Sections[sectionID].m_Mutex);
    #endif
  }
} /* End of 'VESectionEnter' function */

/***
 * PURPOSE: Leave critical section
 *   PARAM: [IN] sectionID - critical section identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionLeave( const VEUINT sectionID )
{
  if ((sectionID > 0)&&(sectionID < p_SectionsMaximalNumber))
  {
    #ifdef _WIN32
    LeaveCriticalSection((CRITICAL_SECTION*)&p_Sections[sectionID].m_Section);
    #else
    pthread_mutex_unlock((pthread_mutex_t*)&p_Sections[sectionID].m_Mutex);
    #endif
  }
} /* End of 'VESectionLeave' function */

/*** Internal use only ***/

/***
 * PURPOSE: Create a critical section for internal use only
 *  RETURN: Created critical section
 *   PARAM: [IN] comment - internal critical section comment
 *  AUTHOR: Eliseev Dmitry
 ***/
VECRITICALSECTION *VESectionCreateInternal( VEBUFFER comment )
{
  VECRITICALSECTION *criticalSection = New(sizeof(VECRITICALSECTION), comment);
  if (!criticalSection)
    return NULL;

  #ifdef _WIN32
  InitializeCriticalSection((CRITICAL_SECTION*)&criticalSection->m_Section);
  #else
  memset((VEPOINTER)&criticalSection->m_Mutex, 0, sizeof(pthread_mutex_t));
  #endif

  /* Section created */
  return criticalSection;
} /* End of 'VESectionCreateInternal' function */

/***
 * PURPOSE: Delete an internal critical section
 *   PARAM: [IN] sectionPtr - pointer to created critical section
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionDeleteInternal( VECRITICALSECTION *sectionPtr )
{
  if (!sectionPtr)
    return;

  #ifdef _WIN32
  DeleteCriticalSection((CRITICAL_SECTION*)&sectionPtr->m_Section);
  #endif

  Delete(sectionPtr);
} /* End of 'VESectionDeleteInternal' function */

/***
 * PURPOSE: Enter in internal critical section
 *   PARAM: [IN] sectionPtr - pointer to created critical section
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionEnterInternal( const VECRITICALSECTION *sectionPtr )
{
  if (!sectionPtr)
    return;

  #ifdef _WIN32
  EnterCriticalSection((CRITICAL_SECTION*)&sectionPtr->m_Section);
  #else
  pthread_mutex_lock((pthread_mutex_t*)&sectionPtr->m_Mutex);
  #endif
} /* End of 'VESectionEnterInternal' function */

/***
 * PURPOSE: Leave an internal critical section
 *   PARAM: [IN] sectionPtr - pointer to created critical section
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionLeaveInternal( const VECRITICALSECTION *sectionPtr )
{
  if (!sectionPtr)
    return;

  #ifdef _WIN32
  LeaveCriticalSection((CRITICAL_SECTION*)&sectionPtr->m_Section);
  #else
  pthread_mutex_unlock((pthread_mutex_t*)&sectionPtr->m_Mutex);
  #endif
} /* End of 'VESectionLeaveInternal' function */
