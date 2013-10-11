#include "structuresmanager.h"
#include "memorymanager.h"
#include "internalqueuestack.h"

/***
 * PURPOSE: Create queue/stack node
 *  RETURN: Pointer to created list node if success, NULL otherwise
 *   PARAM: [IN] type - type of structure
 *   PARAM: [IN] data - pointer to data
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUEUESTACK *VEQueueStackCreateInternal( const VEBYTE type, const VEPOINTER data )
{
  VEQUEUESTACK *qs = New(sizeof(VEQUEUESTACK), "Queue / stack");
  if (!qs)
    return NULL;

  qs->m_Type = type;
  qs->m_First = VEListCreateInternal(0, NULL);
  if (!qs->m_First) {
    Delete(qs);
    return NULL;
  }

  qs->m_Last  = qs->m_First;
  return qs;
} /* End of 'VEQueueStackCreateInternal' function */

/***
 * PURPOSE: Copy entire queue/stack to same another one
 *  RETURN: Copied list pointer
 *   PARAM: [IN] list     - pointer to original list
 *   PARAM: [IN] funcCopy - function, that will be applied to each item at current list to copy item
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUEUESTACK *VEQueueStackCopyInternal( const VEQUEUESTACK *list, const VEFUNCPTRPTR funcCopy )
{
  VEQUEUESTACK *qs = NULL;

  /* Wrong arguments */
  if (!(list&&funcCopy))
    return NULL;

  /* Memory allocation */
  qs = New(sizeof(VEQUEUESTACK), "Queue / stack");
  if (!qs)
    return NULL;

  /* Copying data */
  qs->m_First = VEListCopyInternal(list->m_First, funcCopy);
  if (!qs->m_First)
  {
    Delete(qs);
    return NULL;
  }

  /* Moving to last item */
  qs->m_Last = qs->m_First;
  while (qs->m_Last->m_Next)
    qs->m_Last = qs->m_Last->m_Next;

  /* That's it */
  qs->m_Type = list->m_Type;
  return qs;
} /* End of 'VEQueueStackCopyInternal' function */

/***
 * PURPOSE: Put queue/stack node into queue/stack
 *   PARAM: [IN] list - pointer to existing list
 *   PARAM: [IN] node - pointer to existing node
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEQueueStackPutInternal( VEQUEUESTACK *list, VELIST *node )
{
  /* Wrong arguments */
  if (!(list&&node))
    return;

  switch(list->m_Type)
  {
  case VE_STRUCTURE_QUEUE:
    list->m_Last->m_Next = node;
    list->m_Last = node;
    break;
  case VE_STRUCTURE_STACK:
    node->m_Next = list->m_First->m_Next;
    list->m_First->m_Next = node;
    break;
  }
} /* End of 'VEQueueStackPutInternal' function */

/***
 * PURPOSE: Get data by it's identifier
 *  RETURN: Pointer to data if success, NULL otherwise
 *   PARAM: [IN] list   - pointer to existing list
 *   PARAM: [IN] nodeID - node identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEQueueStackGetInternal( const VEQUEUESTACK *list )
{
  /* Wrong arguments */
  if (!list)
    return NULL;

  /* That's it */
  if (list->m_First->m_Next)
    return list->m_First->m_Next->m_Data;
  return NULL;
} /* End of 'VEQueueStackGetInternal' function */

/***
 * PURPOSE: Extract item from a queue/stack
 *  RETURN: Pointer to data
 *   PARAM: [IN] list   - pointer to existing list
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEQueueStackExtractInternal( VEQUEUESTACK *list )
{
  /* Wrong arguments */
  if (!list)
    return NULL;

  if (list->m_First->m_Next)
  {
    VEPOINTER data = list->m_First->m_Next->m_Data;
    VELIST   *temp = list->m_First->m_Next;

    /* It was last item in queue/stack */
    if (temp == list->m_Last)
      list->m_Last = list->m_First;

    list->m_First->m_Next = temp->m_Next;
    Delete(temp);

    /* That's it */
    return data;
  }

  /* Underflow */
  return NULL;
} /* End of 'VEQueueStackExtractInternal' function */

/***
 * PURPOSE: Do some things with each item in a queue/stack
 *   PARAM: [IN] list      - pointer to existing list
 *   PARAM: [IN] funcApply - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEQueueStackForeachInternal( const VEQUEUESTACK *list, const VEFUNCTION funcApply )
{
  VELIST *current = NULL;

  if (!(list&&funcApply))
    return;

  /* For each item */
  current = list->m_First;
  while(current)
  {
    if (current->m_Data)
      funcApply(current->m_Data);
    current = current->m_Next;
  }
} /* End of 'VEQueueStackForeachInternal' function */

/***
 * PURPOSE: Delete queue/stack
 *   PARAM: [IN] list - pointer to existing list
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEQueueStackDeleteInternal( VEQUEUESTACK *list )
{
  if (!list)
    return;

  /* Release memory */
  VEListDeleteInternal(list->m_First);
  Delete(list);
} /* End of 'VEQueueStackDeleteInternal' function */
