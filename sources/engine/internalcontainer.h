#ifndef INTERNALCONTAINER_H_INCLUDED
#define INTERNALCONTAINER_H_INCLUDED

#include "internalcriticalsection.h"
#include "criticalsection.h"
#include "types.h"

typedef struct tagVEINTERNALPOINTER
{
  VEPOINTER m_Ptr;  /* Real pointer */
  VEBOOL    m_Busy; /* Busy flag */
} VEINTERNALPOINTER;

/* Internal container structure definition */
typedef struct tagVEINTERNALCONTAINER
{
  VEUINT             m_Size;     /* Container size (number of array elements) */
  VEUINT             m_NumItems; /* Number of items at container */
  VEPOINTER         *m_Items;    /* Container items */

  VECRITICALSECTION *m_Sync;     /* Critical section to synchronize object */
} VEINTERNALCONTAINER;

/*** Definitions ***/

#define VE_DEFAULT_CONTAINERSIZE 64

/***
 * PURPOSE: Create internal container
 *  RETURN: Created internal container if success, NULL otherwise
 *   PARAM: [IN] size    - initial number of elements in container
 *   PARAM: [IN] comment - container's purpose comment
 *  AUTHOR: Eliseev Dmitry
 ***/
VEINTERNALCONTAINER* VEInternalContainerCreate( const VEUINT size, const VEBUFFER comment );

/***
 * PURPOSE: Delete internal container
 *   PARAM: [IN] containerPtr - pointer to created container
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInternalContainerDelete( const VEINTERNALCONTAINER *containerPtr );

/***
 * PURPOSE: Add item to internal container
 *  RETURN: Added item identifier if success, 0 otherwise
 *   PARAM: [IN] containerPtr - pointer to created container
 *   PARAM: [IN] item         - pointer to item to add
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEInternalContainerAdd( VEINTERNALCONTAINER *containerPtr, const VEPOINTER item );

/***
 * PURPOSE: Remove item from internal container
 *   PARAM: [IN] containerPtr - pointer to created container
 *   PARAM: [IN] itemID       - identifier of item to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInternalContainerRemove( VEINTERNALCONTAINER *containerPtr, const VEUINT itemID );

/***
 * PURPOSE: Make a copy of an item
 *  RETURN: Identifier of item's copy if success, 0 otherwise
 *   PARAM: [IN] containerPtr - pointer to created container
 *   PARAM: [IN] itemID       - identifier of item to copy
 *   PARAM: [IN] copyFunc     - function, that makes a copy of an object
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEInternalContainerCopy( VEINTERNALCONTAINER *containerPtr, const VEUINT itemID, const VEFUNCTIONWITHRESULT copyFunc );

/***
 * PURPOSE: Get pointer to stored item
 *  RETURN: Pointer to stored item if success, NULL otherwise
 *   PARAM: [IN] containerPtr - pointer to created container
 *   PARAM: [IN] itemID       - identifier of item to get
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEInternalContainerGet( const VEINTERNALCONTAINER *containerPtr, const VEUINT itemID );

/***
 * PURPOSE: Apply function for each item at container
 *   PARAM: [IN] containerPtr - pointer to created container
 *   PARAM: [IN] func         - pointer to function to apply
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInternalContainerForeach( const VEINTERNALCONTAINER *containerPtr, const VEFUNCTION func );

/***
 * PURPOSE: Apply function for each item at container
 *   PARAM: [IN] containerPtr - pointer to created container
 *   PARAM: [IN] func         - pointer to function with two arguments to apply
 *   PARAM: [IN] arg          - pointer to argument
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEInternalContainerForeachWithArgument( const VEINTERNALCONTAINER *containerPtr, const VEFUNCTION2 func, const VEPOINTER arg );

/***
 * PURPOSE: Remove items from current data structure using filter function. If function return TRUE, item will be deleted
 *  RETURN: Number of filtered items
 *   PARAM: [IN] array      - pointer to existing array
 *   PARAM: [IN] funcFilter - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEInternalContainerFilter( VEINTERNALCONTAINER *containerPtr, const VEFUNCTIONFILTER funcFilter );

#endif // INTERNALCONTAINER_H_INCLUDED
