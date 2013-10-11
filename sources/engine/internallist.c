#include "memorymanager.h"
#include "internallist.h"

/***
 * PURPOSE: Create list node
 *  RETURN: Pointer to created list node if success, NULL otherwise
 *   PARAM: [IN] nodeID - node identifier
 *   PARAM: [IN] data   - pointer to data
 *  AUTHOR: Eliseev Dmitry
 ***/
VELIST *VEListCreateInternal( const VEUINT nodeID, const VEPOINTER data )
{
  VELIST *node = New(sizeof(VELIST), "List node creation");
  if (!node)
    return NULL;

  node->m_Data = data;
  node->m_ID   = nodeID;

  return node;
} /* End of 'VEListCreateInternal' function */

/***
 * PURPOSE: Copy entire list to same another one
 *  RETURN: Copied list pointer
 *   PARAM: [IN] list     - pointer to original list
 *   PARAM: [IN] funcCopy - function, that will be applied to each item at current list to copy item
 *  AUTHOR: Eliseev Dmitry
 ***/
VELIST *VEListCopyInternal( const VELIST *list, const VEFUNCPTRPTR funcCopy )
{
  VELIST *copiedList = NULL, *currentNodeOrig = NULL, *currentNodeDest = NULL;

  if (!(list&&funcCopy))
    return NULL;

  copiedList = VEListCreateInternal(0, NULL);

  currentNodeOrig = list->m_Next;
  currentNodeDest = copiedList;

  /* Copy list */
  while (currentNodeOrig)
  {
    currentNodeDest->m_Next = VEListCreateInternal(currentNodeOrig->m_ID, funcCopy(currentNodeOrig->m_Data));

    currentNodeOrig = currentNodeOrig->m_Next;
    currentNodeDest = currentNodeDest->m_Next;
  }

  /* List was copied */
  return copiedList;
} /* End of 'VEListCopyInternal' function */

/***
 * PURPOSE: Put list node into list
 *   PARAM: [IN] list - pointer to existing list
 *   PARAM: [IN] node - pointer to existing node
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEListPutInternal( VELIST *list, VELIST *node )
{
  if (!(list&&node))
    return;

  node->m_Next = list->m_Next;
  list->m_Next = node;
} /* End of 'VEListPutInternal' function */

/***
 * PURPOSE: Get data by it's identifier
 *  RETURN: Pointer to data if success, NULL otherwise
 *   PARAM: [IN] list   - pointer to existing list
 *   PARAM: [IN] nodeID - node identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEListGetInternal( const VELIST *list, const VEUINT nodeID )
{
  VELIST *current = (VELIST*)list;

  while(current)
  {
    if (current->m_ID == nodeID)
      return current->m_Data;
    current = current->m_Next;
  }

  /* Data not found */
  return NULL;
} /* End of 'VEListGetInternal' function */

/***
 * PURPOSE: Extract item from a list
 *  RETURN: Pointer to data
 *   PARAM: [IN] list   - pointer to existing list
 *   PARAM: [IN] itemID - item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEListExtractInternal( const VELIST *list, const VEUINT itemID )
{
  VELIST *current = NULL, *prev = (VELIST*)list;

  /* Wrong identifier */
  if ((itemID == 0)||(list == NULL))
    return NULL;

  current = list->m_Next;
  while (current)
  {
    if (current->m_ID == itemID)
    {
      VEPOINTER *data = current->m_Data;
      prev->m_Next = current->m_Next;

      Delete(current);
      return data;
    }

    prev = current;
    current = current->m_Next;
  }

  /* Item not found */
  return NULL;
} /* End of 'VEListExtractInternal' function */

/***
 * PURPOSE: Do some things with each item in a list
 *   PARAM: [IN] list      - pointer to existing list
 *   PARAM: [IN] funcApply - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEListForeachInternal( const VELIST *list, const VEFUNCTION funcApply )
{
  VELIST *current = NULL;
  if (!(list&&funcApply))
    return;
  current = list->m_Next;

  while(current)
  {
    funcApply(current->m_Data);
    current = current->m_Next;
  }
} /* End of 'VEListForeachInternal' function */

/***
 * PURPOSE: Remove items from current data structure using filter function. If function return TRUE, item will be deleted
 *  RETURN: Number of filtered items
 *   PARAM: [IN] list       - pointer to existing list
 *   PARAM: [IN] funcFilter - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEListFilterInternal( const VELIST *list, const VEFUNCTIONFILTER funcFilter )
{
  VEUINT numFiltered = 0;
  VELIST *current = NULL, *prev = (VELIST*)list;

  /* Wrong identifier */
  if (!(list&&funcFilter))
    return 0;

  current = list->m_Next;
  while (current)
  {
    if (funcFilter(current->m_Data))
    {
      numFiltered++;
      prev->m_Next = current->m_Next;
      Delete(current);
      current = prev;
    }

    prev = current;
    current = current->m_Next;
  }

  /* Return number of filered items */
  return numFiltered;
} /* End of 'VEListFilterInternal' function */

/***
 * PURPOSE: Delete list
 *   PARAM: [IN] list - pointer to existing list
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEListDeleteInternal( VELIST *list )
{
  VELIST *current = NULL;

  if (!list)
    return;

  while (list)
  {
    current = list;
    list = list->m_Next;
    Delete(current);
  }
} /* End of 'VEListDeleteInternal' function */
