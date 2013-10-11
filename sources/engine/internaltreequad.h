#ifndef TREEQUAD_H_INCLUDED
#define TREEQUAD_H_INCLUDED

#include "vector2d.h"

/* Quad tree node */
typedef struct tagVETREEQUAD
{
  VEVECTOR2D m_XZMin; /* Point with minimal X & Z */
  VEVECTOR2D m_XZMax; /* Point with maximal X & Z */

  VEPOINTER  m_Data;  /* Node data */

  /* Children nodes */
  struct tagVETREEQUAD *m_Children[4];
} VETREEQUAD;

/***
 * PURPOSE: Create QUAD node
 *  RETURN: Pointer to created BST node if success, NULL otherwise
 *   PARAM: [IN] minXZ - point with minimal X & Z
 *   PARAM: [IN] maxXZ - point with maximal X & Z
 *   PARAM: [IN] data  - pointer to data
 *  AUTHOR: Eliseev Dmitry
 ***/
VETREEQUAD *VETreeQUADCreateInternal( const VEVECTOR2D minXZ, const VEVECTOR2D maxXZ, const VEPOINTER data );

/***
 * PURPOSE: Create an empty QUAD node using LOD information
 *  RETURN: Pointer to created BST node if success, NULL otherwise
 *   PARAM: [IN] minXZ    - point with minimal X & Z
 *   PARAM: [IN] maxXZ    - point with maximal X & Z
 *   PARAM: [IN] levelLOD - LOD information
 *  AUTHOR: Eliseev Dmitry
 ***/
VETREEQUAD *VETreeQUADCreateByLODInternal( const VEVECTOR2D minXZ, const VEVECTOR2D maxXZ, const VEUINT levelLOD );

/***
 * PURPOSE: Do some things with each item in a tree
 *   PARAM: [IN] tree      - pointer to existing tree
 *   PARAM: [IN] funcApply - function, that will be applied to each item at current tree
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeQUADForeachInternal( const VETREEQUAD *tree, const VEFUNCTION funcApply );

/***
 * PURPOSE: Delete tree
 *   PARAM: [IN] tree - pointer to existing tree
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeQUADDeleteInternal( VETREEQUAD *tree );

#endif // TREEQUAD_H_INCLUDED
