#ifndef STRUCTURESMANAGER_H_INCLUDED
#define STRUCTURESMANAGER_H_INCLUDED

#include "types.h"

/*** Supported structures ***/

/* Array */
#define VE_STRUCTURE_ARRAY 0

/* List */
#define VE_STRUCTURE_LIST  1

/* Queue */
#define VE_STRUCTURE_QUEUE 2

/* Stack */
#define VE_STRUCTURE_STACK 3

/***
 *        Complexity of operations depends on used data structure. See table below. This is complexity in worst.
 *
 *        PUT       GET       EXTRACT    REMOVE    SIZE    COPY    FOREACH    FILTER
 * ARRAY  O(N)      O(1)      O(1)       O(1)      O(1)    O(N)    O(N)       O(N)
 *  LIST  O(1)      O(N)      O(N)       O(N)      O(1)    O(N)    O(N)       O(N)
 * QUEUE  O(1)      O(1)      O(1)        -        O(1)    O(N)    O(N)       O(N)
 * STACK  O(1)      O(1)      O(1)        -        O(1)    O(N)    O(N)       O(N)
 *
 ***/

/***
 * PURPOSE: Create new data structure
 *  RETURN: Created data structure identifier if success, 0 otherwise
 *   PARAM: [IN] structureType - type of data structure (see VE_STRUCTURE_XXX definitions)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStructureCreate( const VEBYTE structureType );

/***
 * PURPOSE: Copy entire structure to same another one
 *  RETURN: Copied data structure identifier
 *   PARAM: [IN] structureID - structure identifier to copy
 *   PARAM: [IN] funcCopy    - function, that will be applied to each item at current data structure to copy item
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStructureCopy( const VEUINT structureID, const VEFUNCPTRPTR funcCopy );

/***
 * PURPOSE: Add item to a structure
 *  RETURN: Added item identifier if success, 0 otherwise
 *   PARAM: [IN] structureID - structure identifier
 *   PARAM: [IN] data        - pointer to data
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStructurePut( const VEUINT structureID, const VEPOINTER data );

/***
 * PURPOSE: Get item from a structure
 *  RETURN: Pointer to data
 *   PARAM: [IN] structureID - structure identifier
 *   PARAM: [IN] itemID      - item identifier (not used if queue or stack)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEStructureGet( const VEUINT structureID, const VEUINT itemID );

/***
 * PURPOSE: Extract item from a structure
 *  RETURN: Pointer to data
 *   PARAM: [IN] structureID - structure identifier
 *   PARAM: [IN] itemID      - item identifier (not used if queue or stack)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEStructureExtract( const VEUINT structureID, const VEUINT itemID );

/***
 * PURPOSE: Remove item from a structure
 *   PARAM: [IN] structureID - structure identifier
 *   PARAM: [IN] itemID      - item identifier (not used if queue or stack)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStructureRemove( const VEUINT structureID, const VEUINT itemID );

/***
 * PURPOSE: Do some things with each item in a structure
 *   PARAM: [IN] structureID - structure identifier to copy
 *   PARAM: [IN] funcApply - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStructureForeach( const VEUINT structureID, const VEFUNCTION funcApply );

/***
 * PURPOSE: Remove items from current data structure using filter function. If function return TRUE, item will be deleted
 * COMMENT: Not works for queue and stack, returns always 0
 *  RETURN: Number of filtered items
 *   PARAM: [IN] structureID - structure identifier to copy
 *   PARAM: [IN] funcFilter  - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStructureFilter( const VEUINT structureID, const VEFUNCTIONFILTER funcFilter );

/***
 * PURPOSE: Determine total number of items in data structure
 *  RETURN: Number of items in data structure if success, 0 otherwise
 *   PARAM: [IN] structureID - structure identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStructureGetSize( const VEUINT structureID );

/***
 * PURPOSE: Delete existing data structure
 *   PARAM: [IN] structureID - created data structure identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStructureDelete( const VEUINT structureID );

#endif // STRUCTURESMANAGER_H_INCLUDED
