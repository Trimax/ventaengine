#include "internalstructuresmanager.h"
#include "internaltreemanager.h"
#include "internalcontainer.h"
#include "internaltreebst.h"
#include "memorymanager.h"
#include "treemanager.h"


/* Structures manager */
volatile static VEINTERNALCONTAINER *p_TreesContainer = NULL;

/***
 * PURPOSE: Initialize trees manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETreeInit( VEVOID )
{
  if (p_TreesContainer)
    return TRUE;

  /* Container creation */
  p_TreesContainer = VEInternalContainerCreate(VE_DEFAULT_STRUCTURES, "Trees manager");
  if (!p_TreesContainer)
    return FALSE;

  return TRUE;
} /* End of 'VETreeInit' function */

/***
 * PURPOSE: Create new tree internal
 *  RETURN: Pointer to created data structure if success, NULL otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VESTRUCTURE *VETreeCreateInternal( VEVOID )
{
  VESTRUCTURE *structure = New(sizeof(VESTRUCTURE), "Tree container");
  if (!structure)
    return NULL;

  structure->m_Sync = VESectionCreateInternal("Tree critical section");
  if (!structure->m_Sync)
  {
    Delete(structure);
    return NULL;
  }

  return structure;
} /* End of 'VETreeCreateInternal' function */

/***
 * PURPOSE: Create new tree
 *  RETURN: Created tree identifier if success, 0 otherwise
 *   PARAM: [IN] treeType - type of tree (see VE_TREE_XXX definitions)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VETreeCreate( const VEBYTE treeType )
{
  VESTRUCTURE *structure = NULL;

  if (!p_TreesContainer)
    return 0;

  structure = VETreeCreateInternal();
  if (!structure)
    return 0;

  structure->m_Type = treeType;
  switch(treeType)
  {
  case VE_TREE_BST:
    structure->m_Structure = VETreeBSTCreateInternal(0, NULL);
    break;

  default:
    Delete(structure);
    return 0;
  }

  /* Add item to container */
  return VEInternalContainerAdd((VEINTERNALCONTAINER*)p_TreesContainer, structure);
} /* End of 'VETreeCreate' function */

/***
 * PURPOSE: Delete existing tree
 *   PARAM: [IN] treeID - created tree identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeDelete( const VEUINT treeID )
{
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_TreesContainer, treeID);
  if (!structure)
    return;

  /* Remove structure from container */
  VEInternalContainerRemove((VEINTERNALCONTAINER*)p_TreesContainer, treeID);
  VESectionEnterInternal(structure->m_Sync);

  /* Delete structure-related data */
  switch(structure->m_Type)
  {
  case VE_TREE_BST:
    VETreeBSTDeleteInternal(structure->m_Structure);
    break;
  }

  /* Release structure */
  VESectionLeaveInternal(structure->m_Sync);
  VESectionDeleteInternal(structure->m_Sync);
  Delete(structure);
} /* End of 'VETreeDelete' function */

/***
 * PURPOSE: Copy entire tree to same another one
 *  RETURN: Copied tree identifier
 *   PARAM: [IN] treeID   - tree identifier to copy
 *   PARAM: [IN] funcCopy - function, that will be applied to each item at current tree to copy item
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VETreeCopy( const VEUINT treeID, const VEFUNCPTRPTR funcCopy )
{
  VESTRUCTURE *copiedStructure = NULL;
  VEUINT copiedStructureID = 0;
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_TreesContainer, treeID);
  if (!structure)
    return 0;

  /* Remove structure from container */
  VESectionEnterInternal(structure->m_Sync);

  /* Copying structure */
  copiedStructureID = VETreeCreate(structure->m_Type);
  if (copiedStructureID == 0)
  {
    VESectionLeaveInternal(structure->m_Sync);
    return 0;
  }

  /* Get created structure pointer */
  copiedStructure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_TreesContainer, copiedStructureID);
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
  case VE_TREE_BST:
    copiedStructure->m_Structure = VETreeBSTCopyInternal(structure->m_Structure, funcCopy, NULL);
    break;
  }

  VESectionLeaveInternal(structure->m_Sync);
  return copiedStructureID;
} /* End of 'VETreeCopy' function */

/***
 * PURPOSE: Add item to a tree
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] treeID - tree identifier
 *   PARAM: [IN] itemID - item identifier
 *   PARAM: [IN] data        - pointer to data
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETreePut( const VEUINT treeID, const VEUINT itemID, const VEPOINTER data )
{
  VEBOOL result = TRUE;
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_TreesContainer, treeID);
  if (!structure)
    return FALSE;

  /* Remove structure from container */
  VESectionEnterInternal(structure->m_Sync);

  /* Delete structure-related data */
  switch(structure->m_Type)
  {
  case VE_TREE_BST:
    result = VETreeBSTPutInternal(structure->m_Structure, data, itemID);
    break;
  }

  /* Data sucessfully added */
  if (result)
    structure->m_Size++;

  VESectionLeaveInternal(structure->m_Sync);
  return TRUE;
} /* End of 'VETreePut' function */

/***
 * PURPOSE: Get item from a tree
 *  RETURN: Pointer to data
 *   PARAM: [IN] treeID - tree identifier
 *   PARAM: [IN] itemID - item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VETreeGet( const VEUINT treeID, const VEUINT itemID )
{
  VEPOINTER item = NULL;
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_TreesContainer, treeID);
  if (!structure)
    return NULL;

  /* Remove structure from container */
  VESectionEnterInternal(structure->m_Sync);

  /* Delete structure-related data */
  switch(structure->m_Type)
  {
  case VE_TREE_BST:
    item = VETreeBSTGetInternal(structure->m_Structure, itemID);
    break;
  }

  VESectionLeaveInternal(structure->m_Sync);
  return item;
} /* End of 'VETreeGet' function */

/***
 * PURPOSE: Extract item from a tree
 *  RETURN: Pointer to data
 *   PARAM: [IN] treeID - tree identifier
 *   PARAM: [IN] itemID - item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VETreeExtract( const VEUINT treeID, const VEUINT itemID )
{
  VEPOINTER item = NULL;
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_TreesContainer, treeID);
  if (!structure)
    return NULL;

  /* Remove structure from container */
  VESectionEnterInternal(structure->m_Sync);

  /* Delete structure-related data */
  switch(structure->m_Type)
  {
  case VE_TREE_BST:
    item = VETreeBSTExtractInternal(structure->m_Structure, itemID);
    break;
  }

  /* Item sucessfully removed */
  if (item != NULL)
    structure->m_Size--;

  VESectionLeaveInternal(structure->m_Sync);
  return item;
} /* End of 'VETreeExtract' function */

/***
 * PURPOSE: Remove item from a tree
 *   PARAM: [IN] treeID - tree identifier
 *   PARAM: [IN] itemID - item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeRemove( const VEUINT treeID, const VEUINT itemID )
{
  VEPOINTER item = NULL;
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_TreesContainer, treeID);
  if (!structure)
    return;

  /* Remove structure from container */
  VESectionEnterInternal(structure->m_Sync);

  /* Delete structure-related data */
  switch(structure->m_Type)
  {
  case VE_TREE_BST:
    item = VETreeBSTExtractInternal(structure->m_Structure, itemID);
    break;
  }

  /* Item sucessfully removed */
  if (item != NULL)
    structure->m_Size--;

  VESectionLeaveInternal(structure->m_Sync);
} /* End of 'VETreeRemove' function */

/***
 * PURPOSE: Do some things with each item in a tree
 *   PARAM: [IN] treeID    - tree identifier to copy
 *   PARAM: [IN] funcApply - function, that will be applied to each item at current tree
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeForeach( const VEUINT treeID, const VEFUNCTION funcApply )
{
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_TreesContainer, treeID);
  if (!structure)
    return;

  /* Remove structure from container */
  VESectionEnterInternal(structure->m_Sync);

  /* Delete structure-related data */
  switch(structure->m_Type)
  {
  case VE_TREE_BST:
    VETreeBSTForeachInternal(structure->m_Structure, funcApply);
    break;
  }

  VESectionLeaveInternal(structure->m_Sync);
} /* End of 'VETreeForeach' function */

/***
 * PURPOSE: Determine total number of items in tree
 *  RETURN: Number of items in tree if success, 0 otherwise
 *   PARAM: [IN] treeID - tree identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VETreeGetSize( const VEUINT treeID )
{
  VESTRUCTURE *structure = VEInternalContainerGet((VEINTERNALCONTAINER*)p_TreesContainer, treeID);
  if (!structure)
    return 0;

  return structure->m_Size;
} /* End of 'VETreeGetSize' function */

/***
 * PURPOSE: Deinitialize trees manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeDeinit( VEVOID )
{
  if (!p_TreesContainer)
    return;

  VEInternalContainerDelete((VEINTERNALCONTAINER*)p_TreesContainer);
  p_TreesContainer = NULL;
} /* End of 'VETreeDeinit' function */
