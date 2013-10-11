#include "internalcriticalsection.h"
#include "memorymanager.h"
#include "internaltask.h"
#include "thread.h"
#include "logger.h"

#include <stdlib.h>
#include <string.h>
#include <stdio.h>

/*** Threads ***/
volatile static VETHREAD *p_Threads = NULL;

/* Threads manager initialization state */
volatile static VEBOOL p_ThreadsInitialized = FALSE;

/* Maximal number of threads */
volatile static VEUINT p_ThreadsMaximalNumber = 0;

/*** Thread function ***/
#ifdef _WIN32
DWORD WINAPI ThreadFunction( VEPOINTER ID )
#else
VEVOID ThreadFunction( VEPOINTER ID)
#endif
{
  VEUINT threadID = *(VEUINT*)ID;

  /* Start user's method */
  p_Threads[threadID].m_Task.m_Func(p_Threads[threadID].m_Task.m_Data);

  VESectionSystemEnter();
  p_Threads[threadID].m_Used = FALSE;
  VESectionSystemLeave();

  #ifdef _WIN32
  return TRUE;
  #endif
} /* End of 'ThreadFunction' function */

/***
 * PURPOSE: Initialize threads manager
 *   PARAM: [IN] maximalThreadsNumber - maximal allowed threads
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEThreadInit( const VEUINT maximalThreadsNumber )
{
  if (maximalThreadsNumber == 0)
  {
    p_ThreadsInitialized = TRUE;
    return TRUE;
  }

  /* Memory allocation */
  p_Threads = New(sizeof(VETHREAD) * maximalThreadsNumber, "Threads manager initialization");
  if (!p_Threads)
    return FALSE;

  p_ThreadsMaximalNumber = maximalThreadsNumber;
  p_ThreadsInitialized = TRUE;
  return TRUE;
} /* End of 'VEThreadInit' function */

/***
 * PURPOSE: Deinitialize threads manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEThreadDeinit( VEVOID )
{
  VEUINT threadID;

  if (p_Threads)
    for (threadID = 0; threadID < p_ThreadsMaximalNumber; threadID++)
      VEThreadJoin(threadID);

  if (p_Threads)
    Delete((VEPOINTER)p_Threads);

  p_Threads = NULL;
  p_ThreadsMaximalNumber = 0;
  p_ThreadsInitialized = FALSE;
} /* End of 'VEThreadDeinit' function */

/***
 * PURPOSE: Find first free thread container
 *  RETURN: Thread container identifier if success, 0 otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT ThreadSelect( VEVOID )
{
  VEUINT threadID = 0, currentID = 0;

  VESectionSystemEnter();
  for (currentID = 1; (currentID < p_ThreadsMaximalNumber)&&(threadID == 0); currentID++)
    if (!p_Threads[currentID].m_Used)
    {
      threadID = currentID;
      p_Threads[threadID].m_Used = TRUE;
    }
  VESectionSystemLeave();

  /* Thread selected */
  return threadID;
} /* End of 'ThreadSelect' function */

/***
 * PURPOSE: Create new thread
 *  RETURN: Newly created thread identifier if success, 0 otherwise
 *   PARAM: [IN] function - function, that will be called from new thread
 *   PARAM: [IN] data     - user's data, that will be passed to function
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEThreadCreate( const VEFUNCTION function, const VEPOINTER data )
{
  VEUINT threadID = 0;
  if (!p_ThreadsInitialized)
    return 0;

  if (function == NULL)
    return 0;

  /* Try to select thread */
  threadID = ThreadSelect();
  if (threadID == 0)
    return 0;

  p_Threads[threadID].m_ID = threadID;
  p_Threads[threadID].m_Task.m_Func = function;
  p_Threads[threadID].m_Task.m_Data = data;

  #ifdef _WIN32
  p_Threads[threadID].m_Thread = CreateThread(NULL, 0, ThreadFunction, (VEPOINTER)&p_Threads[threadID].m_ID, 0, NULL);
  #else
  pthread_create((pthread_t*)&p_Threads[threadID].m_Thread, NULL, (void*(*)(void*))ThreadFunction, (VEPOINTER)&p_Threads[threadID].m_ID);
  #endif

  /* Thread created */
  return threadID;
} /* End of 'ThreadCreate' function */

/***
 * PURPOSE: Delete existing thread
 *   PARAM: [IN] threadID - existing thread identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEThreadDelete( const VEUINT threadID )
{
  if (!p_ThreadsInitialized)
    return;

  if ((threadID > 0)&&(threadID < p_ThreadsMaximalNumber))
  {
    #ifdef _WIN32
    TerminateThread((HANDLE)p_Threads[threadID].m_Thread, 0);
    #else
    pthread_cancel((pthread_t)p_Threads[threadID].m_Thread);
    #endif

    p_Threads[threadID].m_ID = 0;
    p_Threads[threadID].m_Used = FALSE;
  }
} /* End of 'ThreadDelete' function */

/***
 * PURPOSE: Join existing thread
 *   PARAM: [IN] threadID - existing thread identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEThreadJoin( const VEUINT threadID )
{
  if (!p_ThreadsInitialized)
    return;

  if ((threadID > 0)&&(threadID < p_ThreadsMaximalNumber))
  {
    VECHAR message[VE_BUFFER_SMALL];
    if (p_Threads[threadID].m_Thread)
    {
      memset(message, 0, VE_BUFFER_SMALL);
      sprintf(message, "Waiting for thread: %d", threadID);
      VELoggerInfo(message);
    }

    #ifdef _WIN32
    WaitForSingleObject(p_Threads[threadID].m_Thread, INFINITE);
    #else
    if (p_Threads[threadID].m_Thread != 0)
      pthread_join(p_Threads[threadID].m_Thread, NULL);
    #endif

    if (p_Threads[threadID].m_Thread)
    {
      memset(message, 0, VE_BUFFER_SMALL);
      sprintf(message, "Thread %d finished work", threadID);
      VELoggerInfo(message);
    }
  }
} /* End of 'ThreadJoin' function */
