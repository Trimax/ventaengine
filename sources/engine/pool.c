#include "internalcriticalsection.h"
#include "memorymanager.h"
#include "internalcore.h"
#include "internaltask.h"
#include "thread.h"
#include "pool.h"
#include "time.h"

/*** Threads ***/
volatile static VEPOOL *p_Pools = NULL;

/* Threads manager initialization state */
volatile static VEBOOL p_PoolsInitialized = FALSE;

/* Maximal number of threads */
volatile static VEUINT p_PoolsMaximalNumber = 0;

/***
 * PURPOSE: Initialize pool manager
 *   PARAM: [IN] maximalPoolsNumber - maximal allowed pools
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEPoolInit( const VEUINT maximalPoolsNumber )
{
  if (maximalPoolsNumber == 0)
  {
    p_PoolsInitialized = TRUE;
    return TRUE;
  }

  /* Memory allocation */
  p_Pools = New(sizeof(VEPOOL) * maximalPoolsNumber, "Pools manager initialization");
  if (!p_Pools)
    return FALSE;

  p_PoolsMaximalNumber = maximalPoolsNumber;
  p_PoolsInitialized = TRUE;
  return TRUE;
} /* End of 'VEPoolInit' function */

/***
 * PURPOSE: Deinitialize pool manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEPoolDeinit( VEVOID )
{
  VEUINT poolID = 0;
  if (p_Pools)
    for (poolID = 0; poolID < p_PoolsMaximalNumber; poolID++)
      VEPoolDelete(poolID);

  if (p_Pools)
    Delete((VEPOINTER)p_Pools);

  p_Pools = NULL;
  p_PoolsMaximalNumber = 0;
  p_PoolsInitialized = FALSE;
} /* End of 'VEPoolDeinit' function */

/***
 * PURPOSE: Find first free thread container
 *  RETURN: Thread container identifier if success, 0 otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT PoolSelect( VEVOID )
{
  VEUINT poolID = 0, currentID = 0;

  VESectionSystemEnter();
  for (currentID = 1; (currentID < p_PoolsMaximalNumber)&&(poolID == 0); currentID++)
    if (!p_Pools[currentID].m_Used)
    {
      poolID = currentID;
      p_Pools[poolID].m_Used = TRUE;
    }
  VESectionSystemLeave();

  /* Thread selected */
  return poolID;
} /* End of 'PoolSelect' function */

/***
 * PURPOSE: Task runner
 *   PARAM: [IN] ID - pool identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID PoolFunction( VEPOINTER ID )
{
  VEUINT poolID = *(VEUINT*)ID;

  /* While pool is active */
  while ((p_Pools[poolID].m_Active)&&(!VEIsStopping()))
  {
    VETASK *currentTask = NULL;

    /* Extract task from queue */
    VESectionSystemEnter();
    if (p_Pools[poolID].m_Queue->m_Next)
    {
      currentTask = p_Pools[poolID].m_Queue->m_Next;
      p_Pools[poolID].m_Queue->m_Next = currentTask->m_Next;
    }
    VESectionSystemLeave();

    /* There is a task */
    if (currentTask)
      currentTask->m_Func(currentTask->m_Data);

    VEWait(VE_POOL_WAIT);
  }

  /* Decrease number of runned threads */
  p_Pools[poolID].m_Runned--;
  if (p_Pools[poolID].m_Runned == 0)
  {
    VETASK *current = p_Pools[poolID].m_Queue, *prev = NULL;

    /* Remove tasks queue */
    while (current)
    {
      prev = current;
      current = current->m_Next;

      if (prev)
        Delete((VEPOINTER)prev);
    }

    /* Queue deleted */
    p_Pools[poolID].m_Queue = NULL;

    /* Delete threads array */
    Delete((VEPOINTER)p_Pools[poolID].m_Threads);
    p_Pools[poolID].m_Threads = NULL;

    p_Pools[poolID].m_ID = 0;
  }
} /* End of 'PoolFunction' function */

/***
 * PURPOSE: Create new pool
 *  RETURN: Newly created thread identifier if success, 0 otherwise
 *   PARAM: [IN] size - pool size
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEPoolCreate( const VEUINT size )
{
  VEUINT poolID = 0, threadID = 0;
  if (!p_PoolsInitialized)
    return 0;

  /* Wrong size */
  if (size == 0)
    return 0;

  /* Try to select pool */
  poolID = PoolSelect();
  if (poolID == 0)
    return 0;

  p_Pools[poolID].m_Size  = size;
  p_Pools[poolID].m_Queue = New(sizeof(VETASK), "Pool queue");
  if (!p_Pools[poolID].m_Queue)
  {
    p_Pools[poolID].m_Used = FALSE;
    return 0;
  }

  p_Pools[poolID].m_Threads = New(sizeof(VEUINT) * size, "Pool threads array");
  if (!p_Pools[poolID].m_Threads)
  {
    Delete(p_Pools[poolID].m_Queue);
    p_Pools[poolID].m_Used = FALSE;
    return 0;
  }

  p_Pools[poolID].m_Active = TRUE;
  p_Pools[poolID].m_ID = poolID;
  for (threadID = 0; threadID < size; threadID++)
    p_Pools[poolID].m_Threads[threadID] = VEThreadCreate(PoolFunction, (VEPOINTER)&p_Pools[poolID].m_ID);

  /* Pool created */
  p_Pools[poolID].m_Runned = size;
  return poolID;
} /* End of 'PoolCreate' function */

/***
 * PURPOSE: Delete existing pool
 *   PARAM: [IN] poolID - existing pool identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEPoolDelete( const VEUINT poolID )
{
  if (!p_PoolsInitialized)
    return;

  if ((poolID > 0)&&(poolID < p_PoolsMaximalNumber))
    p_Pools[poolID].m_Active = FALSE;

  /* Waiting for pool */
  while (p_Pools[poolID].m_ID != 0)
    VEWait(VE_POOL_WAIT);

  p_Pools[poolID].m_Used = FALSE;
} /* End of 'PoolDelete' function */

/***
 * PURPOSE: Add new task to pool
 *   PARAM: [IN] poolID   - existing pool identifier
 *   PARAM: [IN] function - function, that will be called from new thread
 *   PARAM: [IN] data     - user's data, that will be passed to function
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEPoolPush( const VEUINT poolID, const VEFUNCTION function, const VEPOINTER data )
{
  if (!function)
    return;

  if ((poolID > 0)&&(poolID < p_PoolsMaximalNumber))
    if (p_Pools[poolID].m_Queue)
    {
      VETASK *newTask = New(sizeof(VETASK), "Task for pool");
      if (!newTask)
        return;

      newTask->m_Func = function;
      newTask->m_Data = data;

      VESectionSystemEnter();
      newTask->m_Next = p_Pools[poolID].m_Queue->m_Next;
      p_Pools[poolID].m_Queue->m_Next = newTask;
      VESectionSystemLeave();
    }
} /* End of 'PoolPush' function */
