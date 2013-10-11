#include "internalstructuresmanager.h"
#include "internalcontainer.h"
#include "structuresmanager.h"
#include "memorymanager.h"

/* Data structures */
#include "internalqueuestack.h"
#include "internalarray.h"
#include "internallist.h"

/* Structures manager */
volatile static VEINTERNALCONTAINER *p_StructuresContainer = NULL;

/***
 * PURPOSE: Initialize structures manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEStructureInit( VEVOID )
{
  if (p_StructuresContainer)
    return TRUE;

  /* Container creation */
  p_StructuresContainer = VEInternalContainerCreate(VE_DEFAULT_STRUCTURES, "Structures manager");
  if (!p_StructuresContainer)
    return FALSE;

  return TRUE;
} /* End of 'VEStructureInit' function */

/***
 * PURPOSE: Create new data structure internal
 *  RETURN: Pointer to created data structure if success, NULL otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VESTRUCTURE *VEStructureCreateInternal( VEVOID )
{
  VESTRUCTURE *structure = New(sizeof(VESTRUCTURE), "Data structure");
  if (!structure)
    return NULL;

  structure->m_Sync = VESectionCreateInternal("Data structure critical section");
  if (!structure->m_Sync)
  {
    Delete(structure);
    return NULL;
  }

  return structure;
} /* End of 'VEStructureCreateInternal' function */

/***
 * PURPOSE: Create new data structure
 *  RETURN: Created data structure identifier if success, 0 otherwise
 *   PARAM: [IN] structureType - type of data structure (see VE_STRUCTURE_XXX definitions)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStructureCreate( const VEBYTE structureType )
{
  VESTRUCTURE *structure = NULL;

  if (!p_StructuresContainer)
    return 0;

  structure = VEStructureCreateInternal();
  if (!structure)
    return 0;

  structure->m_Type = structureType;
  switch(structureType)
  {
  case VE_STRUCTURE_ARRAY:
    structure->m_Structure = VEArrayCreateInternal(VE_DEFAULT_ARRAYSIZE);
    break;

  case VE_STRUCTURE_LIST:
    structure->m_Structure = VEListCreateInternal(0, NULL);
    break;

  case VE_STRUCTURE_QUEUE:
    structure->m_Structure = VEQueueStackCreateInternal(structureType, NULL);
    break;

  case VE_STRUCTURE_STACK:
    structure->m_Structure = VEQueueStackCreateInternal(structureType, NULL);
    break;

  default:
    Delete(structure);
    return 0;
  }

  /* Add item to container */
  return VEInternalContainerAdd((VEINTERNALCONTAINER*)p_StructuresContainer, structure);
} /* End of 'VEStructureCreate' function */

/***
 * PURPOSE: Delete existing data structure
 *   PARAM: [IN] structureID - created data structure identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStructureDelete( const VEUINT structureID )
{
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StructuresContainer, structureID);
  if (!structure)
    return;

  /* Remove structure from container */
  VEInternalContainerRemove((VEINTERNALCONTAINER*)p_StructuresContainer, structureID);
  VESectionEnterInternal(structure->m_Sync);

  /* Delete structure-related data */
  switch(structure->m_Type)
  {
  case VE_STRUCTURE_ARRAY:
    VEArrayDeleteInternal(structure->m_Structure);
    break;

  case VE_STRUCTURE_LIST:
    VEListDeleteInternal(structure->m_Structure);
    break;

  case VE_STRUCTURE_QUEUE:
    VEQueueStackDeleteInternal(structure->m_Structure);
    break;

  case VE_STRUCTURE_STACK:
    VEQueueStackDeleteInternal(structure->m_Structure);
    break;
  }

  /* Release structure */
  VESectionLeaveInternal(structure->m_Sync);
  VESectionDeleteInternal(structure->m_Sync);
  Delete(structure);
} /* End of 'VEStructureDelete' function */

/***
 * PURPOSE: Copy entire structure to same another one
 *  RETURN: Copied data structure identifier
 *   PARAM: [IN] structureID - structure identifier to copy
 *   PARAM: [IN] funcCopy    - function, that will be applied to each item at current data structure to copy item
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStructureCopy( const VEUINT structureID, const VEFUNCPTRPTR funcCopy )
{
  VESTRUCTURE *copiedStructure = NULL;
  VEUINT copiedStructureID = 0;
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StructuresContainer, structureID);
  if (!structure)
    return 0;

  /* Remove structure from container */
  VESectionEnterInternal(structure->m_Sync);

  /* Copying structure */
  copiedStructureID = VEStructureCreate(structure->m_Type);
  if (copiedStructureID == 0)
  {
    VESectionLeaveInternal(structure->m_Sync);
    return 0;
  }

  /* Get created structure pointer */
  copiedStructure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StructuresContainer, copiedStructureID);
  if (!copiedStructure)
  {
    VESectionLeaveInternal(structure->m_Sync);
    return 0;
  }

  /* Copying parameters */
  copiedStructure->m_Size = structure->m_Size;
  copiedStructure->m_LastID    = structure->m_LastID;

  /* Copy data */
  switch(structure->m_Type)
  {
  case VE_STRUCTURE_ARRAY:
    copiedStructure->m_Structure = VEArrayCopyInternal(structure->m_Structure, funcCopy);
    break;

  case VE_STRUCTURE_LIST:
    copiedStructure->m_Structure = VEListCopyInternal(structure->m_Structure, funcCopy);
    break;

  case VE_STRUCTURE_QUEUE:
    copiedStructure->m_Structure = VEQueueStackCopyInternal(structure->m_Structure, funcCopy);
    break;

  case VE_STRUCTURE_STACK:
    copiedStructure->m_Structure = VEQueueStackCopyInternal(structure->m_Structure, funcCopy);
    break;
  }

  VESectionLeaveInternal(structure->m_Sync);
  return copiedStructureID;
} /* End of 'VEStructureCopy' function */

/***
 * PURPOSE: Add item to a structure
 *  RETURN: Added item identifier if success, 0 otherwise
 *   PARAM: [IN] structureID - structure identifier
 *   PARAM: [IN] data        - pointer to data
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStructurePut( const VEUINT structureID, const VEPOINTER data )
{
  VEUINT itemID = 0;
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StructuresContainer, structureID);
  if (!structure)
    return 0;

  /* Remove structure from container */
  VESectionEnterInternal(structure->m_Sync);

  /* Delete structure-related data */
  switch(structure->m_Type)
  {
  case VE_STRUCTURE_ARRAY:
    itemID = VEArrayPutInternal(structure->m_Structure, data);
    break;

  case VE_STRUCTURE_LIST:
    {
      VELIST *newNode = VEListCreateInternal(structure->m_LastID + 1, data);
      if (newNode)
      {
        VEListPutInternal(structure->m_Structure, newNode);
        itemID = ++structure->m_LastID;
      }
    }
    break;

  case VE_STRUCTURE_QUEUE:
    {
      VELIST *newNode = VEListCreateInternal(structure->m_LastID + 1, data);
      if (newNode)
      {
        VEQueueStackPutInternal(structure->m_Structure, newNode);
        itemID = ++structure->m_LastID;
      }
    }
    break;

  case VE_STRUCTURE_STACK:
    {
      VELIST *newNode = VEListCreateInternal(structure->m_LastID + 1, data);
      if (newNode)
      {
        VEQueueStackPutInternal(structure->m_Structure, newNode);
        itemID = ++structure->m_LastID;
      }
    }
    break;
  }

  /* Data sucessfully added */
  if (itemID != 0)
    structure->m_Size++;

  VESectionLeaveInternal(structure->m_Sync);
  return itemID;
} /* End of 'VEStructurePut' function */

/***
 * PURPOSE: Get item from a structure
 *  RETURN: Pointer to data
 *   PARAM: [IN] structureID - structure identifier
 *   PARAM: [IN] itemID      - item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEStructureGet( const VEUINT structureID, const VEUINT itemID )
{
  VEPOINTER item = NULL;
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StructuresContainer, structureID);
  if (!structure)
    return NULL;

  /* Remove structure from container */
  VESectionEnterInternal(structure->m_Sync);

  /* Delete structure-related data */
  switch(structure->m_Type)
  {
  case VE_STRUCTURE_ARRAY:
    item = VEArrayGetInternal(structure->m_Structure, itemID);
    break;

  case VE_STRUCTURE_LIST:
    item = VEListGetInternal(structure->m_Structure, itemID);
    break;

  case VE_STRUCTURE_QUEUE:
    item = VEQueueStackGetInternal(structure->m_Structure);
    break;

  case VE_STRUCTURE_STACK:
    item = VEQueueStackGetInternal(structure->m_Structure);
    break;
  }

  VESectionLeaveInternal(structure->m_Sync);
  return item;
} /* End of 'VEStructureGet' function */

/***
 * PURPOSE: Extract item from a structure
 *  RETURN: Pointer to data
 *   PARAM: [IN] structureID - structure identifier
 *   PARAM: [IN] itemID      - item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEStructureExtract( const VEUINT structureID, const VEUINT itemID )
{
  VEPOINTER item = NULL;
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StructuresContainer, structureID);
  if (!structure)
    return NULL;

  /* Remove structure from container */
  VESectionEnterInternal(structure->m_Sync);

  /* Delete structure-related data */
  switch(structure->m_Type)
  {
  case VE_STRUCTURE_ARRAY:
    item = VEArrayExtractInternal(structure->m_Structure, itemID);
    break;

  case VE_STRUCTURE_LIST:
    item = VEListExtractInternal(structure->m_Structure, itemID);
    break;

  case VE_STRUCTURE_QUEUE:
    item = VEQueueStackExtractInternal(structure->m_Structure);
    break;

  case VE_STRUCTURE_STACK:
    item = VEQueueStackExtractInternal(structure->m_Structure);
    break;
  }

  /* Item sucessfully removed */
  if (item != NULL)
    structure->m_Size--;

  VESectionLeaveInternal(structure->m_Sync);
  return item;
} /* End of 'VEStructureExtract' function */

/***
 * PURPOSE: Remove item from a structure
 *   PARAM: [IN] structureID - structure identifier
 *   PARAM: [IN] itemID      - item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStructureRemove( const VEUINT structureID, const VEUINT itemID )
{
  VEPOINTER item = NULL;
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StructuresContainer, structureID);
  if (!structure)
    return;

  /* Remove structure from container */
  VESectionEnterInternal(structure->m_Sync);

  /* Delete structure-related data */
  switch(structure->m_Type)
  {
  case VE_STRUCTURE_ARRAY:
    item = VEArrayExtractInternal(structure->m_Structure, itemID);
    break;

  case VE_STRUCTURE_LIST:
    item = VEListExtractInternal(structure->m_Structure, itemID);
    break;

  case VE_STRUCTURE_QUEUE:
    item = VEQueueStackExtractInternal(structure->m_Structure);
    break;

  case VE_STRUCTURE_STACK:
    item = VEQueueStackExtractInternal(structure->m_Structure);
    break;
  }

  /* Item sucessfully removed */
  if (item != NULL)
    structure->m_Size--;

  VESectionLeaveInternal(structure->m_Sync);
} /* End of 'VEStructureRemove' function */

/***
 * PURPOSE: Do some things with each item in a structure
 *   PARAM: [IN] structureID - structure identifier to copy
 *   PARAM: [IN] funcApply - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStructureForeach( const VEUINT structureID, const VEFUNCTION funcApply )
{
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StructuresContainer, structureID);
  if (!structure)
    return;

  /* Remove structure from container */
  VESectionEnterInternal(structure->m_Sync);

  /* Delete structure-related data */
  switch(structure->m_Type)
  {
  case VE_STRUCTURE_ARRAY:
    VEArrayForeachInternal(structure->m_Structure, funcApply);
    break;

  case VE_STRUCTURE_LIST:
    VEListForeachInternal(structure->m_Structure, funcApply);
    break;

  case VE_STRUCTURE_QUEUE:
    VEQueueStackForeachInternal(structure->m_Structure, funcApply);
    break;

  case VE_STRUCTURE_STACK:
    VEQueueStackForeachInternal(structure->m_Structure, funcApply);
    break;
  }

  VESectionLeaveInternal(structure->m_Sync);
} /* End of 'VEStructureForeach' function */

/***
 * PURPOSE: Remove items from current data structure using filter function. If function return TRUE, item will be deleted
 *  RETURN: Number of filtered items
 *   PARAM: [IN] structureID - structure identifier to copy
 *   PARAM: [IN] funcFilter  - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStructureFilter( const VEUINT structureID, const VEFUNCTIONFILTER funcFilter )
{
  VEUINT numFiltered = 0;
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StructuresContainer, structureID);
  if (!structure)
    return 0;

  /* Remove structure from container */
  VESectionEnterInternal(structure->m_Sync);

  /* Delete structure-related data */
  switch(structure->m_Type)
  {
  case VE_STRUCTURE_ARRAY:
    numFiltered = VEArrayFilterInternal(structure->m_Structure, funcFilter);
    break;

  case VE_STRUCTURE_LIST:
    numFiltered = VEListFilterInternal(structure->m_Structure, funcFilter);
    break;
  }

  structure->m_Size -= numFiltered;

  VESectionLeaveInternal(structure->m_Sync);
  return numFiltered;
} /* End of 'VEStructureFilter' function */

/***
 * PURPOSE: Determine total number of items in data structure
 *  RETURN: Number of items in data structure if success, 0 otherwise
 *   PARAM: [IN] structureID - structure identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStructureGetSize( const VEUINT structureID )
{
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StructuresContainer, structureID);
  if (!structure)
    return 0;

  return structure->m_Size;
} /* End of 'VEStructureGetSize' function */

/***
 * PURPOSE: Deinitialize structures manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStructureDeinit( VEVOID )
{
  if (!p_StructuresContainer)
    return;

  VEInternalContainerDelete((VEINTERNALCONTAINER*)p_StructuresContainer);
  p_StructuresContainer = NULL;
} /* End of 'VEStructureDeinit' function */
