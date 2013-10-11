#include "internalcontainer.h"
#include "memorymanager.h"

#include <string.h>
#include <stdio.h>

/***
 * PURPOSE: Create internal container
 *  RETURN: Created internal container if success, NULL otherwise
 *   PARAM: [IN] size    - initial number of elements in container
 *   PARAM: [IN] comment - container's purpose comment
 *  AUTHOR: Eliseev Dmitry
 ***/
VEINTERNALCONTAINER* VEInternalContainerCreate( const VEUINT size, const VEBUFFER comment )
{
  VECHAR sectionComment[VE_BUFFER_STANDART];
  VEINTERNALCONTAINER *newContainer = NULL;

  if (size == 0)
    return NULL;

  newContainer = New(sizeof(VEINTERNALCONTAINER), comment);
  if (!newContainer)
    return NULL;

  memset(sectionComment, 0, VE_BUFFER_STANDART);
  sprintf(sectionComment, "ICS: %s", comment);

  newContainer->m_Sync = VESectionCreateInternal(sectionComment);
  if (!newContainer->m_Sync)
  {
    Delete(newContainer);
    return NULL;
  }

  newContainer->m_Items = New(sizeof(VEPOINTER) * size, "Internal container items");
  if (!newContainer->m_Items)
  {
    VESectionDeleteInternal(newContainer->m_Sync);
    Delete(newContainer);
    return NULL;
  }

  /* Storing size */
  newContainer->m_Size = size;
  return newContainer;
} /* End of 'VEInternalContainerCreate' function */

/***
 * PURPOSE: Delete internal container
 *   PARAM: [IN] containerPtr - pointer to created container
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInternalContainerDelete( const VEINTERNALCONTAINER *containerPtr )
{
  if (!containerPtr)
    return;

  VESectionEnterInternal(containerPtr->m_Sync);
  Delete(containerPtr->m_Items);
  VESectionLeaveInternal(containerPtr->m_Sync);

  VESectionDeleteInternal(containerPtr->m_Sync);

  Delete((VEPOINTER)containerPtr);
} /* End of 'VEInternalContainerDelete' function */

/***
 * PURPOSE: Add item to internal container
 *  RETURN: Added item identifier if success, 0 otherwise
 *   PARAM: [IN] containerPtr - pointer to created container
 *   PARAM: [IN] item - pointer to item to add
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEInternalContainerAdd( VEINTERNALCONTAINER *containerPtr, const VEPOINTER item )
{
  VEUINT itemID = 0, pos = 0;

  if (!containerPtr)
    return 0;

  VESectionEnterInternal(containerPtr->m_Sync);

  /* Do we need reallocate items array */
  if ((containerPtr->m_NumItems+1) == containerPtr->m_Size)
  {
    VEPOINTER *newItemsArray = New(sizeof(VEPOINTER) * containerPtr->m_Size * 2, "Reallocated internal container items");
    memcpy(newItemsArray, containerPtr->m_Items, sizeof(VEPOINTER) * containerPtr->m_Size);
    Delete(containerPtr->m_Items);
    containerPtr->m_Items = newItemsArray;
    containerPtr->m_Size *= 2;
  }

  /* Find first free place */
  for (pos = 1; (pos < containerPtr->m_Size)&&(itemID == 0); pos++)
    if (!containerPtr->m_Items[pos])
      itemID = pos;

  containerPtr->m_Items[itemID] = item;
  containerPtr->m_NumItems++;

  VESectionLeaveInternal(containerPtr->m_Sync);

  return itemID;
} /* End of 'VEInternalContainerAdd' function */

/***
 * PURPOSE: Remove item from internal container
 *   PARAM: [IN] containerPtr - pointer to created container
 *   PARAM: [IN] itemID - identifier of item to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInternalContainerRemove( VEINTERNALCONTAINER *containerPtr, const VEUINT itemID )
{
  if (!containerPtr)
    return;

  VESectionEnterInternal(containerPtr->m_Sync);

  if ((itemID > 0)&&(itemID < containerPtr->m_Size))
    if (containerPtr->m_Items[itemID])
    {
      containerPtr->m_Items[itemID] = NULL;
      containerPtr->m_NumItems--;
    }

  VESectionLeaveInternal(containerPtr->m_Sync);
} /* End of 'VEInternalContainerRemove' function */

/***
 * PURPOSE: Get pointer to stored item
 *  RETURN: Pointer to stored item if success, NULL otherwise
 *   PARAM: [IN] containerPtr - pointer to created container
 *   PARAM: [IN] itemID - identifier of item to get
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEInternalContainerGet( const VEINTERNALCONTAINER *containerPtr, const VEUINT itemID )
{
  VEPOINTER *item = NULL;

  if (!containerPtr)
    return NULL;

  VESectionEnterInternal(containerPtr->m_Sync);

  if ((itemID > 0)&&(itemID < containerPtr->m_Size))
    item = containerPtr->m_Items[itemID];

  VESectionLeaveInternal(containerPtr->m_Sync);

  return item;
} /* End of 'VEInternalContainerGet' function */

/***
 * PURPOSE: Make a copy of an item
 *  RETURN: Identifier of item's copy if success, 0 otherwise
 *   PARAM: [IN] containerPtr - pointer to created container
 *   PARAM: [IN] itemID       - identifier of item to copy
 *   PARAM: [IN] copyFunc     - function, that makes a copy of an object
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEInternalContainerCopy( VEINTERNALCONTAINER *containerPtr, const VEUINT itemID, const VEFUNCTIONWITHRESULT copyFunc )
{
  VEPOINTER *copiedItem = NULL;
  if (!(containerPtr&&copyFunc))
    return 0;

  VESectionEnterInternal(containerPtr->m_Sync);

  if ((itemID > 0)&&(itemID < containerPtr->m_Size))
    if (containerPtr->m_Items[itemID])
      copiedItem = copyFunc(containerPtr->m_Items[itemID]);

  VESectionLeaveInternal(containerPtr->m_Sync);

  if (!copiedItem)
    return 0;

  return VEInternalContainerAdd(containerPtr, copiedItem);
} /* End of 'VEInternalContainerCopy' function */

/***
 * PURPOSE: Apply function for each item at container
 *   PARAM: [IN] containerPtr - pointer to created container
 *   PARAM: [IN] func         - pointer to function to apply
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInternalContainerForeach( const VEINTERNALCONTAINER *containerPtr, const VEFUNCTION func )
{
  VEUINT pos = 0;

  if (!containerPtr)
    return;

  VESectionEnterInternal(containerPtr->m_Sync);
  for (pos = 0; pos < containerPtr->m_Size; pos++)
    if (containerPtr->m_Items[pos])
      func(containerPtr->m_Items[pos]);

  VESectionLeaveInternal(containerPtr->m_Sync);
} /* End of 'VEInternalContainerForeach' function */

/***
 * PURPOSE: Apply function for each item at container
 *   PARAM: [IN] containerPtr - pointer to created container
 *   PARAM: [IN] func         - pointer to function with two arguments to apply
 *   PARAM: [IN] arg          - pointer to argument
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInternalContainerForeachWithArgument( const VEINTERNALCONTAINER *containerPtr, const VEFUNCTION2 func, const VEPOINTER arg )
{
  VEUINT pos = 0;

  if (!containerPtr)
    return;

  VESectionEnterInternal(containerPtr->m_Sync);
  for (pos = 0; pos < containerPtr->m_Size; pos++)
    if (containerPtr->m_Items[pos])
      func(containerPtr->m_Items[pos], arg);

  VESectionLeaveInternal(containerPtr->m_Sync);
} /* End of 'VEInternalContainerForeachWithArgument' function */

/***
 * PURPOSE: Remove items from current data structure using filter function. If function return TRUE, item will be deleted
 *  RETURN: Number of filtered items
 *   PARAM: [IN] array      - pointer to existing array
 *   PARAM: [IN] funcFilter - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEInternalContainerFilter( VEINTERNALCONTAINER *containerPtr, const VEFUNCTIONFILTER funcFilter )
{
  VEUINT numFiltered = 0, itemID = 0;

  if (!(containerPtr&&funcFilter))
    return 0;

  VESectionEnterInternal(containerPtr->m_Sync);
  for (itemID = 0; itemID < containerPtr->m_Size; itemID++)
    if (containerPtr->m_Items[itemID])
      if (funcFilter(containerPtr->m_Items[itemID]))
      {
        containerPtr->m_Items[itemID] = NULL;
        containerPtr->m_NumItems--;
        numFiltered++;
      }
  VESectionLeaveInternal(containerPtr->m_Sync);

  return numFiltered;
} /* End of 'VEInternalContainerFilter' function */
