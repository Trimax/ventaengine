#ifndef QUEUESTACK_H_INCLUDED
#define QUEUESTACK_H_INCLUDED

#include "internallist.h"

/* Stack / queue */
typedef struct tagVEQUEUESTACK
{
  VEBYTE  m_Type;   /* Structure type */
  VELIST *m_First;  /* Pointer to first item */
  VELIST *m_Last;   /* Pointer to last item */
} VEQUEUESTACK;

/***
 * PURPOSE: Create queue/stack node
 *  RETURN: Pointer to created list node if success, NULL otherwise
 *   PARAM: [IN] type - type of structure
 *   PARAM: [IN] data - pointer to data
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUEUESTACK *VEQueueStackCreateInternal( const VEBYTE type, const VEPOINTER data );

/***
 * PURPOSE: Copy entire queue/stack to same another one
 *  RETURN: Copied list pointer
 *   PARAM: [IN] list     - pointer to original list
 *   PARAM: [IN] funcCopy - function, that will be applied to each item at current list to copy item
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUEUESTACK *VEQueueStackCopyInternal( const VEQUEUESTACK *list, const VEFUNCPTRPTR funcCopy );

/***
 * PURPOSE: Put queue/stack node into queue/stack
 *   PARAM: [IN] list - pointer to existing list
 *   PARAM: [IN] node - pointer to existing node
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEQueueStackPutInternal( VEQUEUESTACK *list, VELIST *node );

/***
 * PURPOSE: Get data by it's identifier
 *  RETURN: Pointer to data if success, NULL otherwise
 *   PARAM: [IN] list   - pointer to existing list
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEQueueStackGetInternal( const VEQUEUESTACK *list );

/***
 * PURPOSE: Extract item from a queue/stack
 *  RETURN: Pointer to data
 *   PARAM: [IN] list   - pointer to existing list
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEQueueStackExtractInternal( VEQUEUESTACK *list );

/***
 * PURPOSE: Do some things with each item in a queue/stack
 *   PARAM: [IN] list      - pointer to existing list
 *   PARAM: [IN] funcApply - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEQueueStackForeachInternal( const VEQUEUESTACK *list, const VEFUNCTION funcApply );

/***
 * PURPOSE: Delete queue/stack
 *   PARAM: [IN] list - pointer to existing list
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEQueueStackDeleteInternal( VEQUEUESTACK *list );

#endif // QUEUESTACK_H_INCLUDED
