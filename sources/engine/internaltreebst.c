#include "internaltreebst.h"
#include "memorymanager.h"

/***
 * PURPOSE: Create BST node
 *  RETURN: Pointer to created BST node if success, NULL otherwise
 *   PARAM: [IN] nodeID - node identifier
 *   PARAM: [IN] data   - pointer to data
 *  AUTHOR: Eliseev Dmitry
 ***/
VETREEBST *VETreeBSTCreateInternal( const VEUINT nodeID, const VEPOINTER data )
{
  VETREEBST *node = New(sizeof(VETREEBST), "BST node creation");
  if (!node)
    return NULL;

  node->m_Data = data;
  node->m_ID   = nodeID;

  return node;
} /* End of 'VETreeBSTCreateInternal' function */

/***
 * PURPOSE: Restore parent pointers after copying
 *   PARAM: [IN] tree   - pointer to original BST
 *   PARAM: [IN] parent - pointer to node parent
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeBSTRestoreParentsInternal( VETREEBST *tree, VETREEBST *parent )
{
  if (!tree)
    return;

  tree->m_Parent = parent;

  VETreeBSTRestoreParentsInternal(tree->m_Left, tree);
  VETreeBSTRestoreParentsInternal(tree->m_Right, tree);
} /* End of 'VETreeRestoreParentsInternal' function */

/***
 * PURPOSE: Copy entire BST to same another one
 *  RETURN: Copied BST pointer
 *   PARAM: [IN] tree     - pointer to original BST
 *   PARAM: [IN] funcCopy - function, that will be applied to each item at current list to copy item
 *   PARAM: [IN] parent   - pointer to parent
 *  AUTHOR: Eliseev Dmitry
 ***/
VETREEBST *VETreeBSTCopyInternal( const VETREEBST *tree, const VEFUNCPTRPTR funcCopy, const VETREEBST *parent )
{
  VETREEBST *copiedBST = NULL;

  if (!(tree&&funcCopy))
    return NULL;

  copiedBST = VETreeBSTCreateInternal(tree->m_ID, funcCopy(tree->m_Data));
  copiedBST->m_Parent = (VETREEBST*)tree;

  copiedBST->m_Left  = VETreeBSTCopyInternal(tree->m_Left, funcCopy, tree);
  copiedBST->m_Right = VETreeBSTCopyInternal(tree->m_Right, funcCopy, tree);

  /* List was copied */
  return copiedBST;
} /* End of 'VETreeBSTCopyInternal' function */

/***
 * PURPOSE: Put node into tree
 *   PARAM: [IN] list - pointer to existing tree
 *   PARAM: [IN] data - pointer to data
 *   PARAM: [IN] id   - node identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETreeBSTPutInternal( VETREEBST *tree, VEPOINTER *data, const VEUINT id )
{
  if (!(tree&&data))
    return FALSE;

  if (tree->m_ID == id)
  {
    tree->m_Data = data;
    return TRUE;
  } else if (id > tree->m_ID)
  {
    if (!tree->m_Right)
    {
      tree->m_Right = VETreeBSTCreateInternal(id, data);
      tree->m_Right->m_Parent = tree;
      return TRUE;
    } else
      return VETreeBSTPutInternal(tree->m_Right, data, id);
  }

  if (!tree->m_Left)
  {
    tree->m_Left = VETreeBSTCreateInternal(id, data);
    tree->m_Left->m_Parent = tree;
    return TRUE;
  }

  return VETreeBSTPutInternal(tree->m_Left, data, id);
} /* End of 'VEListPutInternal' function */

/***
 * PURPOSE: Get data by it's identifier
 *  RETURN: Pointer to data if success, NULL otherwise
 *   PARAM: [IN] tree   - pointer to existing tree
 *   PARAM: [IN] nodeID - node identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VETreeBSTGetInternal( const VETREEBST *tree, const VEUINT nodeID )
{
  if (!tree)
    return NULL;

  if (tree->m_ID == nodeID)
    return tree->m_Data;
  else if (nodeID > tree->m_ID)
    return VETreeBSTGetInternal(tree->m_Right, nodeID);
  return VETreeBSTGetInternal(tree->m_Left, nodeID);
} /* End of 'VETreeBSTGetInternal' function */

/***
 * PURPOSE: Extract item from a list
 *  RETURN: Pointer to data
 *   PARAM: [IN] tree   - pointer to existing tree
 *   PARAM: [IN] itemID - item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VETreeBSTExtractInternal( const VETREEBST *tree, const VEUINT itemID )
{
  //TO DO: Implement according to Cormen

  /* Item not found */
  return NULL;
} /* End of 'VETreeBSTExtractInternal' function */

/***
 * PURPOSE: Do some things with each item in a tree
 *   PARAM: [IN] tree      - pointer to existing tree
 *   PARAM: [IN] funcApply - function, that will be applied to each item at current tree
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeBSTForeachInternal( const VETREEBST *tree, const VEFUNCTION funcApply )
{
  if (!tree)
    return;

  funcApply(tree->m_Data);
  VETreeBSTForeachInternal(tree->m_Left, funcApply);
  VETreeBSTForeachInternal(tree->m_Right, funcApply);
} /* End of 'VEListForeachInternal' function */

/***
 * PURPOSE: Delete tree
 *   PARAM: [IN] tree - pointer to existing tree
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeBSTDeleteInternal( VETREEBST *tree )
{
  if (!tree)
    return;

  VETreeBSTDeleteInternal(tree->m_Left);
  VETreeBSTDeleteInternal(tree->m_Right);

  Delete(tree);
} /* End of 'VEListDeleteInternal' function */
