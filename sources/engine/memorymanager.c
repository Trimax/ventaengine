#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "internalcriticalsection.h"
#include "internalmemorymanager.h"
#include "memorymanager.h"
#include "logger.h"
#include "types.h"

/*** Memory manager settings ***/

/* Initialization flag */
volatile static VEBOOL p_MemoryManagerInitialized = FALSE;

/* Memory blocks list */
volatile static VEMEMORYBLOCK *p_MemoryList = NULL;

/* Amount of used memory */
volatile static VEULONG p_MemoryUsed = 0;

/* Minimum macros */
#ifndef MIN
#define MIN(a,b) ((a)<(b))?(a):(b)
#endif

/***
 * PURPOSE: Initialize memory manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEMemoryInit( VEVOID )
{
  /* Manager already initialized */
  if (p_MemoryManagerInitialized)
    return TRUE;

  /* Prepare memory blocks list */
  p_MemoryList = malloc(sizeof(VEMEMORYBLOCK));
  if (!p_MemoryList)
    return FALSE;
  memset((VEPOINTER)p_MemoryList, 0, sizeof(VEMEMORYBLOCK));

  /* Manager initialized */
  p_MemoryManagerInitialized = TRUE;
  return TRUE;
} /* End of 'VEMemoryInit' function */

/***
 * PURPOSE: Deinitialize memory manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMemoryDeinit( VEVOID )
{
  /* Release memory blocks list */
  if (p_MemoryList)
  {
    VEMEMORYBLOCK *currentBlock = NULL;
    while (p_MemoryList)
    {
      currentBlock = (VEMEMORYBLOCK*)p_MemoryList;
      p_MemoryList = p_MemoryList->m_Next;
      free((VEPOINTER)currentBlock->m_Ptr);
      free((VEPOINTER)currentBlock);
    }
  }

  p_MemoryUsed = 0;
  p_MemoryList = NULL;
  p_MemoryManagerInitialized = FALSE;
} /* End of 'VEMemoryDeinit' function */

VEMEMORYBLOCK* MemoryBlockCreate( const VEULONG size, const VEPOINTER ptr, const VEBUFFER comment )
{
  VEMEMORYBLOCK *newMemoryBlock = malloc(sizeof(VEMEMORYBLOCK));
  if (!newMemoryBlock)
    return NULL;
  memset((VEPOINTER)newMemoryBlock, 0, sizeof(VEMEMORYBLOCK));

  newMemoryBlock->m_Size = size;
  newMemoryBlock->m_Ptr  = ptr;

  if (comment != NULL)
    memcpy(newMemoryBlock->m_Comment, comment, MIN(VE_COMMENT_SMALL, strlen(comment)));

  /* Memory block allocated */
  return newMemoryBlock;
} /* End of 'MemoryBlockCreate' function */

/***
 * PURPOSE: Memory allocation
 *  RETURN: Allocated memory if success, NULL otherwise
 *   PARAM: [IN] size    - size of block to allocate (should be a positive number)
 *   PARAM: [IN] comment - comment to allocated block (can be NULL)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER New( const VEULONG size, const VEBUFFER comment )
{
  VEPOINTER newPointer = NULL;
  if (size == 0)
    return NULL;
  newPointer = malloc(size);
  if (!newPointer)
    return NULL;

  /* Zero memory */
  memset(newPointer, 0, size);

  /* Storing allocated memory block information to a list */
  if (p_MemoryManagerInitialized)
  {
    VEMEMORYBLOCK *newMemoryBlock = MemoryBlockCreate(size, newPointer, comment);
    if (!newMemoryBlock)
    {
      free((VEPOINTER)newPointer);
      return NULL;
    }

    /* Adding allocated memory block to list */
    VESectionSystemEnter();
    newMemoryBlock->m_Next = p_MemoryList->m_Next;
    p_MemoryList->m_Next = newMemoryBlock;
    p_MemoryUsed += size;
    VESectionSystemLeave();
  }

  /* That's it */
  return newPointer;
} /* End of 'New' function */

/***
 * PURPOSE: Memory releasing
 *   PARAM: [IN] ptr - pointer to allocated memory block (returned by 'New' function)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID Delete( VEPOINTER ptr )
{
  /* Wrong pointer */
  if (!ptr)
    return;

  /* Removing memory block from list */
  if (p_MemoryManagerInitialized)
  {
    VEMEMORYBLOCK *prev    = (VEMEMORYBLOCK*)p_MemoryList;
    VEMEMORYBLOCK *current = NULL;

    VESectionSystemEnter();

    current = p_MemoryList->m_Next;
    while(current)
    {
      if (current->m_Ptr == ptr)
      {
        p_MemoryUsed -= current->m_Size;
        prev->m_Next = current->m_Next;
        free(current->m_Ptr);
        free(current);
        VESectionSystemLeave();
        return;
      }

      prev = current;
      current = current->m_Next;
    }

    VESectionSystemLeave();
  }

  /* Release memory */
  free(ptr);
} /* End of 'Delete' function */

/***
 * PURPOSE: Get amount of used memory
 *  RETURN: Used memory amount
 *  AUTHOR: Eliseev Dmitry
 ***/
VEULONG MemoryUsed( VEVOID )
{
  return p_MemoryUsed;
} /* End of 'MemoryUsed' function */

/***
 * PURPOSE: Print memory information to file
 *   PARAM: [IN] outFile - file to output memory information
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID MemoryDump( VEVOID )
{
  VECHAR outputBuffer[VE_BUFFER_STANDART];
  VEULONG totalBytes = 0;

  /* Enter system critical section */
  VESectionSystemEnter();

  /* Output information */
  if (p_MemoryManagerInitialized)
  {
    VELoggerInfo("Memory manager dump");

    VEMEMORYBLOCK *current = p_MemoryList->m_Next;
    while(current)
    {
      memset(outputBuffer, 0, VE_BUFFER_STANDART);
      if (current->m_Comment)
      {
        if (current->m_Comment[0] != '#')
        {
          sprintf(outputBuffer, "%ld bytes: %s \"%s\"", current->m_Size, current->m_Comment, (VEBUFFER)current->m_Ptr);
          VELoggerError(outputBuffer);
          totalBytes += current->m_Size;
        }
      } else
      {
        sprintf(outputBuffer, "%ld bytes", current->m_Size);
        VELoggerError(outputBuffer);
        totalBytes += current->m_Size;
      }

      current = current->m_Next;
    }

    /* If there any not released memory */
    if (totalBytes > 0)
    {
      memset(outputBuffer, 0, VE_BUFFER_STANDART);
      sprintf(outputBuffer, "Total bytes not released: %ld", totalBytes);
      VELoggerError(outputBuffer);
    } else
      VELoggerInfo("All memory released!");
  }

  /* Leave system critical section */
  VESectionSystemLeave();
} /* End of 'MemoryDump' function */
