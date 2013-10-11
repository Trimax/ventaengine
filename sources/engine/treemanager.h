#ifndef TREEMANAGER_H_INCLUDED
#define TREEMANAGER_H_INCLUDED

#include "types.h"

/*** Supported trees ***/

/* Binary search tree */
#define VE_TREE_BST 0

/***
 * PURPOSE: Create new tree
 *  RETURN: Created tree identifier if success, 0 otherwise
 *   PARAM: [IN] treeType - type of tree (see VE_TREE_XXX definitions)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VETreeCreate( const VEBYTE treeType );

/***
 * PURPOSE: Copy entire tree to same another one
 *  RETURN: Copied tree identifier
 *   PARAM: [IN] treeID   - tree identifier to copy
 *   PARAM: [IN] funcCopy - function, that will be applied to each item at current tree to copy item
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VETreeCopy( const VEUINT treeID, const VEFUNCPTRPTR funcCopy );

/***
 * PURPOSE: Add item to a tree
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] treeID - tree identifier
 *   PARAM: [IN] itemID - item identifier
 *   PARAM: [IN] data        - pointer to data
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VETreePut( const VEUINT treeID, const VEUINT itemID, const VEPOINTER data );

/***
 * PURPOSE: Get item from a tree
 *  RETURN: Pointer to data
 *   PARAM: [IN] treeID - tree identifier
 *   PARAM: [IN] itemID - item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VETreeGet( const VEUINT treeID, const VEUINT itemID );

/***
 * PURPOSE: Extract item from a tree
 *  RETURN: Pointer to data
 *   PARAM: [IN] treeID - tree identifier
 *   PARAM: [IN] itemID - item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VETreeExtract( const VEUINT treeID, const VEUINT itemID );

/***
 * PURPOSE: Remove item from a tree
 *   PARAM: [IN] treeID - tree identifier
 *   PARAM: [IN] itemID - item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeRemove( const VEUINT treeID, const VEUINT itemID );

/***
 * PURPOSE: Do some things with each item in a tree
 *   PARAM: [IN] treeID    - tree identifier to copy
 *   PARAM: [IN] funcApply - function, that will be applied to each item at current tree
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeForeach( const VEUINT treeID, const VEFUNCTION funcApply );

/***
 * PURPOSE: Determine total number of items in tree
 *  RETURN: Number of items in tree if success, 0 otherwise
 *   PARAM: [IN] treeID - tree identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VETreeGetSize( const VEUINT treeID );

/***
 * PURPOSE: Delete existing tree
 *   PARAM: [IN] treeID - created tree identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeDelete( const VEUINT treeID );

#endif // TREEMANAGER_H_INCLUDED
