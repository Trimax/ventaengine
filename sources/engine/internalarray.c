#include "memorymanager.h"
#include "internalarray.h"

#include <string.h>

/***
 * PURPOSE: Create an array
 *  RETURN: Pointer to created array if success, NULL otherwise
 *   PARAM: [IN] size - startup array size
 *  AUTHOR: Eliseev Dmitry
 ***/
VEARRAY *VEArrayCreateInternal( const VEUINT size )
{
  VEARRAY *array = NULL;

  /* Wrong size */
  if (size == 0)
    return NULL;

  /* Memory allocation */
  array = New(sizeof(VEARRAY), "Array data structure");
  if (!array)
    return NULL;

  array->m_Size = size;
  array->m_Items = New(sizeof(VEPOINTER) * size, "Array items");
  if (!array->m_Items)
  {
    Delete(array);
    return NULL;
  }

  /* That's it */
  return array;
} /* End of 'VEArrayCreateInternal' function */

/***
 * PURPOSE: Copy entire array to same another one
 *  RETURN: Copied array pointer
 *   PARAM: [IN] array    - pointer to original array
 *   PARAM: [IN] funcCopy - function, that will be applied to each item at current array to copy item
 *  AUTHOR: Eliseev Dmitry
 ***/
VEARRAY *VEArrayCopyInternal( const VEARRAY *array, const VEFUNCPTRPTR funcCopy )
{
  VEARRAY *copiedArray = NULL;
  VEUINT i = 0;

  if (!(array && funcCopy))
    return NULL;

  /* Memory allocation */
  copiedArray = VEArrayCreateInternal(array->m_Size);
  if (!copiedArray)
     return NULL;

  /* Copying items */
  copiedArray->m_NumItems = array->m_NumItems;
  for (i = 0; i < array->m_Size; i++)
    if (array->m_Items[i])
      copiedArray->m_Items[i] = funcCopy(array->m_Items[i]);

  return copiedArray;
} /* End of 'VEArrayCopyInternal' function */

/***
 * PURPOSE: Put data into array
 *  RETURN: Putted data identifier if success, 0 otherwise
 *   PARAM: [IN] array - pointer to existing array
 *   PARAM: [IN] data  - pointer to user data
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEArrayPutInternal( VEARRAY *array, const VEPOINTER data )
{
  VEUINT itemID = 0, pos = 0;

  if (!(array&&data))
    return 0;

  /* Do we need reallocate items array */
  if ((array->m_NumItems+1) == array->m_Size)
  {
    VEPOINTER *newItemsArray = New(sizeof(VEPOINTER) * array->m_Size * 2, "Reallocated array items");
    if (!newItemsArray)
      return 0;

    memcpy(newItemsArray, array->m_Items, array->m_Size * sizeof(VEPOINTER));
    Delete(array->m_Items);
    array->m_Items = newItemsArray;
    array->m_Size *= 2;
  }

  /* Find first free place */
  for (pos = 1; (pos < array->m_Size)&&(itemID == 0); pos++)
    if (!array->m_Items[pos])
      itemID = pos;

  /* Add item to an array */
  array->m_Items[itemID] = data;
  array->m_NumItems++;
  return itemID;
} /* End of 'VEArrayPutInternal' function */

/***
 * PURPOSE: Get data by it's identifier
 *  RETURN: Pointer to data if success, NULL otherwise
 *   PARAM: [IN] array  - pointer to existing array
 *   PARAM: [IN] nodeID - node identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEArrayGetInternal( const VEARRAY *array, const VEUINT nodeID )
{
  if (!array)
    return NULL;

  if ((nodeID > 0)&&(nodeID < array->m_Size))
    return array->m_Items[nodeID];
  return NULL;
} /* End of 'VEArrayGetInternal' function */

/***
 * PURPOSE: Extract item from an array
 *  RETURN: Pointer to data
 *   PARAM: [IN] array  - pointer to existing array
 *   PARAM: [IN] itemID - item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEArrayExtractInternal( VEARRAY *array, const VEUINT itemID )
{
  VEPOINTER *item = NULL;

  if (!array)
    return NULL;

  if ((itemID > 0)&&(itemID < array->m_Size))
    item = array->m_Items[itemID];

  if (item)
    array->m_NumItems--;

  array->m_Items[itemID] = NULL;
  return item;
} /* End of 'VEArrayExtractInternal' function */

/***
 * PURPOSE: Do some things with each item in an array
 *   PARAM: [IN] array     - pointer to existing array
 *   PARAM: [IN] funcApply - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEArrayForeachInternal( const VEARRAY *array, const VEFUNCTION funcApply )
{
  VEUINT itemID = 0;

  if (!(array&&funcApply))
    return;

  for (itemID = 0; itemID < array->m_Size; itemID++)
    if (array->m_Items[itemID])
      funcApply(array->m_Items[itemID]);
} /* End of 'VEArrayForeachInternal' function */

/***
 * PURPOSE: Remove items from current data structure using filter function. If function return TRUE, item will be deleted
 *  RETURN: Number of filtered items
 *   PARAM: [IN] array      - pointer to existing array
 *   PARAM: [IN] funcFilter - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEArrayFilterInternal( VEARRAY *array, const VEFUNCTIONFILTER funcFilter )
{
  VEUINT numFiltered = 0, itemID = 0;

  if (!(array&&funcFilter))
    return 0;

  for (itemID = 0; itemID < array->m_Size; itemID++)
    if (array->m_Items[itemID])
      if (funcFilter(array->m_Items[itemID]))
      {
        array->m_Items[itemID] = NULL;
        array->m_NumItems--;
        numFiltered++;
      }

  return numFiltered;
} /* End of 'VEArrayFilterInternal' function */

/***
 * PURPOSE: Delete array
 *   PARAM: [IN] array - pointer to existing array
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEArrayDeleteInternal( VEARRAY *array )
{
  if (!array)
    return;

  Delete(array->m_Items);
  Delete(array);
} /* End of 'VEArrayDeleteInternal' function */
