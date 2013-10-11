#ifndef TREEBST_H_INCLUDED
#define TREEBST_H_INCLUDED

#include "types.h"

/* BST tree node */
typedef struct tagVETREEBST
{
  VEUINT             m_ID;   /* Node identifier */
  VEPOINTER          m_Data; /* Data */

  struct tagVETREEBST *m_Left;   /* Pointer to left child */
  struct tagVETREEBST *m_Right;  /* Pointer to right child */
  struct tagVETREEBST *m_Parent; /* Pointer to parent */
} VETREEBST;

/***
 * PURPOSE: Create BST node
 *  RETURN: Pointer to created BST node if success, NULL otherwise
 *   PARAM: [IN] nodeID - node identifier
 *   PARAM: [IN] data   - pointer to data
 *  AUTHOR: Eliseev Dmitry
 ***/
VETREEBST *VETreeBSTCreateInternal( const VEUINT nodeID, const VEPOINTER data );

/***
 * PURPOSE: Restore parent pointers after copying
 *   PARAM: [IN] tree   - pointer to original BST
 *   PARAM: [IN] parent - pointer to node parent
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeBSTRestoreParentsInternal( VETREEBST *tree, VETREEBST *parent );

/***
 * PURPOSE: Copy entire BST to same another one
 *  RETURN: Copied BST pointer
 *   PARAM: [IN] tree     - pointer to original BST
 *   PARAM: [IN] funcCopy - function, that will be applied to each item at current list to copy item
 *   PARAM: [IN] parent   - pointer to parent
 *  AUTHOR: Eliseev Dmitry
 ***/
VETREEBST *VETreeBSTCopyInternal( const VETREEBST *tree, const VEFUNCPTRPTR funcCopy, const VETREEBST *parent );

/***
 * PURPOSE: Put node into tree
 *   PARAM: [IN] list - pointer to existing tree
 *   PARAM: [IN] data - pointer to data
 *   PARAM: [IN] id   - node identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETreeBSTPutInternal( VETREEBST *tree, VEPOINTER *data, const VEUINT id );

/***
 * PURPOSE: Get data by it's identifier
 *  RETURN: Pointer to data if success, NULL otherwise
 *   PARAM: [IN] tree   - pointer to existing tree
 *   PARAM: [IN] nodeID - node identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VETreeBSTGetInternal( const VETREEBST *tree, const VEUINT nodeID );

/***
 * PURPOSE: Extract item from a list
 *  RETURN: Pointer to data
 *   PARAM: [IN] tree   - pointer to existing tree
 *   PARAM: [IN] itemID - item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VETreeBSTExtractInternal( const VETREEBST *tree, const VEUINT itemID );

/***
 * PURPOSE: Do some things with each item in a tree
 *   PARAM: [IN] tree      - pointer to existing tree
 *   PARAM: [IN] funcApply - function, that will be applied to each item at current tree
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeBSTForeachInternal( const VETREEBST *tree, const VEFUNCTION funcApply );

/***
 * PURPOSE: Delete tree
 *   PARAM: [IN] tree - pointer to existing tree
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeBSTDeleteInternal( VETREEBST *tree );

#endif // TREEBST_H_INCLUDED
