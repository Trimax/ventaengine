#include "internaltreequad.h"
#include "memorymanager.h"

/***
 * PURPOSE: Create QUAD node
 *  RETURN: Pointer to created BST node if success, NULL otherwise
 *   PARAM: [IN] minXZ - point with minimal X & Z
 *   PARAM: [IN] maxXZ - point with maximal X & Z
 *   PARAM: [IN] data  - pointer to data
 *  AUTHOR: Eliseev Dmitry
 ***/
VETREEQUAD *VETreeQUADCreateInternal( const VEVECTOR2D minXZ, const VEVECTOR2D maxXZ, const VEPOINTER data )
{
  VETREEQUAD *node = New(sizeof(VETREEQUAD), "Quad tree vertex");
  if (!node)
    return NULL;

  /* Storing data */
  node->m_XZMin = minXZ;
  node->m_XZMax = maxXZ;
  node->m_Data  = data;

  /* That's it */
  return node;
} /* End of 'VETreeQUADCreateInternal' function */

/***
 * PURPOSE: Do some things with each item in a tree
 *   PARAM: [IN] tree      - pointer to existing tree
 *   PARAM: [IN] funcApply - function, that will be applied to each item at current tree
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeQUADForeachInternal( const VETREEQUAD *tree, const VEFUNCTION funcApply )
{
  if (!(tree&&funcApply))
    return;

  /* Self apply */
  funcApply(tree->m_Data);

  /* Apply to children */
  VETreeQUADForeachInternal(tree->m_Children[0], funcApply);
  VETreeQUADForeachInternal(tree->m_Children[1], funcApply);
  VETreeQUADForeachInternal(tree->m_Children[2], funcApply);
  VETreeQUADForeachInternal(tree->m_Children[3], funcApply);
} /* End of 'VETreeBSTForeachInternal' function */

/***
 * PURPOSE: Delete tree
 *   PARAM: [IN] tree - pointer to existing tree
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETreeQUADDeleteInternal( VETREEQUAD *tree )
{
  if (!tree)
    return;

  /* Delete children */
  VETreeQUADDeleteInternal(tree->m_Children[0]);
  VETreeQUADDeleteInternal(tree->m_Children[1]);
  VETreeQUADDeleteInternal(tree->m_Children[2]);
  VETreeQUADDeleteInternal(tree->m_Children[3]);

  /* Self deletion */
  Delete(tree);
} /* End of 'VETreeQUADDeleteInternal' function */

/***
 * PURPOSE: Create an empty QUAD node using LOD information
 *  RETURN: Pointer to created BST node if success, NULL otherwise
 *   PARAM: [IN] minXZ    - point with minimal X & Z
 *   PARAM: [IN] maxXZ    - point with maximal X & Z
 *   PARAM: [IN] levelLOD - LOD information
 *  AUTHOR: Eliseev Dmitry
 ***/
VETREEQUAD *VETreeQUADCreateByLODInternal( const VEVECTOR2D minXZ, const VEVECTOR2D maxXZ, const VEUINT levelLOD )
{
  VETREEQUAD *tree = VETreeQUADCreateInternal(minXZ, maxXZ, NULL);

  /* LOD creation */
  if (levelLOD > 1)
  {
    VEVECTOR2D middle = VEVector2DAverage(minXZ, maxXZ);

    /*************
     *  1  |  2  *
     *-----------*
     *  0  |  3  *
     *************/

    /* Children creation */
    tree->m_Children[0] = VETreeQUADCreateByLODInternal(minXZ,                             middle,                            levelLOD-1);
    tree->m_Children[1] = VETreeQUADCreateByLODInternal(VEVector2D(minXZ.m_X, middle.m_Y), VEVector2D(middle.m_X, maxXZ.m_Y), levelLOD-1);
    tree->m_Children[2] = VETreeQUADCreateByLODInternal(middle,                            maxXZ,                             levelLOD-1);
    tree->m_Children[3] = VETreeQUADCreateByLODInternal(VEVector2D(middle.m_X, minXZ.m_Y), VEVector2D(maxXZ.m_X, middle.m_Y), levelLOD-1);
  }

  /* That's it */
  return tree;
} /* End of 'VETreeQUADCreateByLODInternal' function */
