#ifndef TASK_H_INCLUDED
#define TASK_H_INCLUDED

#ifdef _WIN32
#include <windows.h>
#else
#include <pthread.h>
#endif

#include "types.h"

/*** Definitions ***/

/* Wait before try to extract next task (ms) */
#define VE_POOL_WAIT 100

/* Task structure definition */
typedef struct tagVETASK
{
  VEPOINTER  m_Data;  /* Thread data */
  VEFUNCTION m_Func;  /* Thread function */

  struct tagVETASK *m_Next; /* Pointer to next task */
} VETASK;

/* Thread structure definition */
typedef struct tagVETHREAD
{
  #ifdef _WIN32
  HANDLE m_Thread;     /* Thread handle (for Windows) */
  #else
  pthread_t m_Thread;  /* Thread handle (for Linux) */
  #endif

  VETASK m_Task;       /* Thread task */
  VEBOOL m_Used;       /* Thread in use flag */
  VEUINT m_ID;         /* Thread identifier (self identifier) */
} VETHREAD;

/* Pool structure */
typedef struct tagVEPOOL
{
  VEUINT  m_Size;    /* Pool size (number of threads) */
  VEUINT *m_Threads; /* Pool threads */

  VEBOOL  m_Used;    /* Pool in use flag */
  VETASK *m_Queue;   /* Tasks queue */
  VEUINT  m_ID;      /* Pool identifier (self identifier) */
  VEBOOL  m_Active;  /* Pool active flag */
  VEUINT  m_Runned;  /* Pool runned threads */
} VEPOOL;

/***
 * PURPOSE: Initialize threads manager
 *   PARAM: [IN] maximalThreadsNumber - maximal allowed threads
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEThreadInit( const VEUINT maximalThreadsNumber );

/***
 * PURPOSE: Deinitialize threads manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEThreadDeinit( VEVOID );

/***
 * PURPOSE: Initialize pool manager
 *   PARAM: [IN] maximalPoolsNumber - maximal allowed pools
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEPoolInit( const VEUINT maximalPoolsNumber );

/***
 * PURPOSE: Deinitialize pool manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEPoolDeinit( VEVOID );

#endif // TASK_H_INCLUDED
