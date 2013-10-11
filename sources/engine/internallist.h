#ifndef INTERNALLIST_H_INCLUDED
#define INTERNALLIST_H_INCLUDED

#include "types.h"

/* List data structure */
typedef struct tagVELIST
{
  VEUINT             m_ID;   /* Node identifier */
  VEPOINTER          m_Data; /* Data */
  struct tagVELIST *m_Next;  /* Pointer to next node */
} VELIST;

/***
 * PURPOSE: Create list node
 *  RETURN: Pointer to created list node if success, NULL otherwise
 *   PARAM: [IN] nodeID - node identifier
 *   PARAM: [IN] data   - pointer to data
 *  AUTHOR: Eliseev Dmitry
 ***/
VELIST *VEListCreateInternal( const VEUINT nodeID, const VEPOINTER data );

/***
 * PURPOSE: Copy entire list to same another one
 *  RETURN: Copied list pointer
 *   PARAM: [IN] list     - pointer to original list
 *   PARAM: [IN] funcCopy - function, that will be applied to each item at current list to copy item
 *  AUTHOR: Eliseev Dmitry
 ***/
VELIST *VEListCopyInternal( const VELIST *list, const VEFUNCPTRPTR funcCopy );

/***
 * PURPOSE: Put list node into list
 *   PARAM: [IN] list - pointer to existing list
 *   PARAM: [IN] node - pointer to existing node
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEListPutInternal( VELIST *list, VELIST *node );

/***
 * PURPOSE: Get data by it's identifier
 *  RETURN: Pointer to data if success, NULL otherwise
 *   PARAM: [IN] list   - pointer to existing list
 *   PARAM: [IN] nodeID - node identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEListGetInternal( const VELIST *list, const VEUINT nodeID );

/***
 * PURPOSE: Extract item from a list
 *  RETURN: Pointer to data
 *   PARAM: [IN] list   - pointer to existing list
 *   PARAM: [IN] itemID - item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEListExtractInternal( const VELIST *list, const VEUINT itemID );

/***
 * PURPOSE: Do some things with each item in a list
 *   PARAM: [IN] list      - pointer to existing list
 *   PARAM: [IN] funcApply - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEListForeachInternal( const VELIST *list, const VEFUNCTION funcApply );

/***
 * PURPOSE: Remove items from current data structure using filter function. If function return TRUE, item will be deleted
 *  RETURN: Number of filtered items
 *   PARAM: [IN] list       - pointer to existing list
 *   PARAM: [IN] funcFilter - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEListFilterInternal( const VELIST *list, const VEFUNCTIONFILTER funcFilter );

/***
 * PURPOSE: Delete list
 *   PARAM: [IN] list - pointer to existing list
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEListDeleteInternal( VELIST *list );

#endif // INTERNALLIST_H_INCLUDED
