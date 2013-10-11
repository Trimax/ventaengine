#ifndef INTERNALARRAY_H_INCLUDED
#define INTERNALARRAY_H_INCLUDED

#include "types.h"

/* Default array initial size */
#define VE_DEFAULT_ARRAYSIZE 256

/* List data structure */
typedef struct tagVEARRAY
{
  VEPOINTER *m_Items;    /* Array of pointer to user data */
  VEUINT     m_NumItems; /* Number of used items */
  VEUINT     m_Size;     /* Current array size */
} VEARRAY;

/***
 * PURPOSE: Create an array
 *  RETURN: Pointer to created array if success, NULL otherwise
 *   PARAM: [IN] size - startup array size
 *  AUTHOR: Eliseev Dmitry
 ***/
VEARRAY *VEArrayCreateInternal( const VEUINT size );

/***
 * PURPOSE: Copy entire array to same another one
 *  RETURN: Copied array pointer
 *   PARAM: [IN] array    - pointer to original array
 *   PARAM: [IN] funcCopy - function, that will be applied to each item at current array to copy item
 *  AUTHOR: Eliseev Dmitry
 ***/
VEARRAY *VEArrayCopyInternal( const VEARRAY *array, const VEFUNCPTRPTR funcCopy );

/***
 * PURPOSE: Put data into array
 *  RETURN: Putted data identifier if success, 0 otherwise
 *   PARAM: [IN] array - pointer to existing array
 *   PARAM: [IN] data  - pointer to user data
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEArrayPutInternal( VEARRAY *array, const VEPOINTER data );

/***
 * PURPOSE: Get data by it's identifier
 *  RETURN: Pointer to data if success, NULL otherwise
 *   PARAM: [IN] array  - pointer to existing array
 *   PARAM: [IN] nodeID - node identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEArrayGetInternal( const VEARRAY *array, const VEUINT nodeID );

/***
 * PURPOSE: Extract item from an array
 *  RETURN: Pointer to data
 *   PARAM: [IN] array  - pointer to existing array
 *   PARAM: [IN] itemID - item identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEPOINTER VEArrayExtractInternal( VEARRAY *array, const VEUINT itemID );

/***
 * PURPOSE: Do some things with each item in an array
 *   PARAM: [IN] array     - pointer to existing array
 *   PARAM: [IN] funcApply - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEArrayForeachInternal( const VEARRAY *array, const VEFUNCTION funcApply );

/***
 * PURPOSE: Remove items from current data structure using filter function. If function return TRUE, item will be deleted
 *  RETURN: Number of filtered items
 *   PARAM: [IN] array      - pointer to existing array
 *   PARAM: [IN] funcFilter - function, that will be applied to each item at current data structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEArrayFilterInternal( VEARRAY *array, const VEFUNCTIONFILTER funcFilter );

/***
 * PURPOSE: Delete array
 *   PARAM: [IN] array - pointer to existing array
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEArrayDeleteInternal( VEARRAY *array );

#endif // INTERNALARRAY_H_INCLUDED
