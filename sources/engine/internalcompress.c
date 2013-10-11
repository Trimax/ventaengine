#include "internalcompress.h"
#include "internalbuffer.h"
#include "memorymanager.h"
#include "internallist.h"

#include <string.h>

/* Huffman symbol */
typedef struct tagVEHUFFMANNODE
{
  VEBYTE m_IsUsed;                   /* Is already in tree */
  VEBYTE m_Code;                     /* Symbol code */
  VEREAL m_Freq;                     /* Symbol frequency */
  struct tagVEHUFFMANNODE *m_Left;  /* Left child */
  struct tagVEHUFFMANNODE *m_Right; /* Right child */
} VEHUFFMANNODE;

/* Huffman code */
typedef struct tagVEHUFFMANCODE
{
  VECHAR m_Bits[255];
} VEHUFFMANCODE;

/* Huffman table */
typedef struct tagVEHUFFMANTABLE
{
  VEHUFFMANCODE m_Symbols[255];
} VEHUFFMANTABLE;

/* Huffman's tree */
typedef struct tagVEHUFFMAN
{
  VEHUFFMANNODE  m_Frequencies[255]; /* Frequencies table */
  VEHUFFMANNODE *m_Tree;             /* Huffman tree */
} VEHUFFMAN;

/***
 * PURPOSE: Delete Huffman's tree
 *   PARAM: [IN] tree - pointer to Huffman's tree to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEHuffmanDeleteTree( VEHUFFMANNODE *node )
{
  if (!node)
    return;

  VEHuffmanDeleteTree(node->m_Left);
  VEHuffmanDeleteTree(node->m_Right);
  Delete(node);
} /* End of 'VEHuffmanDeleteTree' function */

/***
 * PURPOSE: Delete Huffman's table
 *   PARAM: [IN] table - pointer to table to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEHuffmanDelete( VEHUFFMAN *table )
{
  if (!table)
    return;

  /* Delete tree */
  //VEHuffmanDeleteTree(table->m_Tree);

  /* Release memory */
  Delete(table);
} /* End of 'VEHuffmanDelete' function */

/***
 * PURPOSE: Prepare frequencies table
 *  RETURN: Pointer to prepared frequencies table if success, NULL otherwise
 *   PARAM: [IN] input - string to compress
 *  AUTHOR: Eliseev Dmitry
 ***/
VEHUFFMAN *VEHuffmanCreate( const VESTRING *input )
{
  VEUINT pos = 0;
  VEHUFFMAN *table = New(sizeof(VEHUFFMAN), "Huffman table");

  /* Not enough memory */
  if (!table)
    return NULL;

  /* Reset table */
  memset(table, 0, sizeof(VEHUFFMAN));

  /* Calculate occurencies */
  for (pos = 0; pos < input->m_Size; pos++)
    table->m_Frequencies[(VEBYTE)input->m_Data[pos]].m_Freq += 1.0;

  /* Convert to frequencies */
  for (pos = 0; pos < 255; pos++)
  {
    table->m_Frequencies[pos].m_Code = pos;
    table->m_Frequencies[pos].m_Freq /= input->m_Size;
  }

  /* That's it */
  return table;
} /* End of 'VEHuffmanCreateTree' function */

VEVOID VEHuffmanFillTable( const VEHUFFMANNODE *node, const VEBUFFER code, VEHUFFMANTABLE *table, const VECHAR bit )
{
  VECHAR currentCode[255];
  memset(currentCode, 0, 255);
  strcpy(currentCode, code);

  /* Increase code */
  if (bit != -1)
    currentCode[VEBufferLength(currentCode)] = bit;

  if (!(node->m_Left && node->m_Right))
    strcpy(table->m_Symbols[node->m_Code].m_Bits, currentCode);
  else
  {
    if (node->m_Left)
      VEHuffmanFillTable(node->m_Left, currentCode, table, '0');
    if (node->m_Right)
      VEHuffmanFillTable(node->m_Right, currentCode, table, '1');
  }
} /* End of 'VEHuffmanFillTable' function */

/***
 * PURPOSE: Obtain pointer to minimal allowed node
 *  RETURN: Pointer to minimal allowed node if success, NULL otherwise
 *   PARAM: [IN] data - pointer to Huffman's tree
 *  AUTHOR: Eliseev Dmitry
 ***/
VEHUFFMANNODE *VEHuffmanMin( VEHUFFMAN *data )
{
  VEHUFFMANNODE *minNode = NULL;
  VEUINT pos = 0;

  /* Select minimal node */
  for (pos = 0; pos < 255; pos++)
    if (!data->m_Frequencies[pos].m_IsUsed)
      if ((minNode == NULL)||(data->m_Frequencies[pos].m_Freq < minNode->m_Freq))
        minNode = &data->m_Frequencies[pos];

  /* Mark node as used */
  if (minNode)
    minNode->m_IsUsed = TRUE;

  /* That's it */
  return minNode;
} /* End of 'VEHuffmanMin' function */

/***
 * PURPOSE: Build codes table using Huffman's tree
 *  RETURN: Codes table
 *   PARAM: [IN] data - pointer to Huffman's tree
 *  AUTHOR: Eliseev Dmitry
 ***/
VEHUFFMANTABLE VEHuffmanCreateTable( const VESTRING *input )
{
  VEBOOL isEmpty = FALSE;
  VEHUFFMANTABLE table;
  VEHUFFMAN *huffman = NULL;

  /* Tree creation */
  memset(&table, 0, sizeof(VEHUFFMANTABLE));
  huffman = VEHuffmanCreate(input);
  if (!huffman)
    return table;

  while(!isEmpty)
  {
    VEHUFFMANNODE *node1 = VEHuffmanMin(huffman);
    VEHUFFMANNODE *node2 = VEHuffmanMin(huffman);

    /* Are pointers wrong */
    if (!(node1&&node2))
    {
      isEmpty = TRUE;
      continue;
    }

    /* Unite nodes */
    huffman->m_Frequencies[node1->m_Code].m_Left   = node1;
    huffman->m_Frequencies[node1->m_Code].m_Right  = node2;
    huffman->m_Frequencies[node1->m_Code].m_Freq   = node1->m_Freq + node2->m_Freq;
    huffman->m_Frequencies[node1->m_Code].m_IsUsed = FALSE;

    huffman->m_Tree = &huffman->m_Frequencies[node1->m_Code];
  }

  /* Fill table with codes */
  VEHuffmanFillTable(huffman->m_Tree, "", &table, -1);

  /* Release memory */
  VEHuffmanDelete(huffman);

  /* That's it */
  return table;
} /* End of 'VEHuffmanCreateTable' function */

/***
 * PURPOSE: Compress string using Huffman's compression
 *  RETURN: Pointer to compressed string
 *   PARAM: [IN] input - string to compress
 *  AUTHOR: Eliseev Dmitry
 ***/
VESTRING *VEStringCompress( const VESTRING *input )
{
  //VEHUFFMANTABLE table;
  VESTRING *processedString = NULL;

  /* Wrong argument */
  if (!input)
    return NULL;

  /* Wrong size */
  if (input->m_Size == 0)
    return NULL;

  /* Table creation */
  //table = VEHuffmanCreateTable(input);

  /* That's it */
  return processedString;
} /* End of 'VEStringCompress' function */

/***
 * PURPOSE: Decompress string using Huffman's decompression
 *  RETURN: Pointer to decompressed string
 *   PARAM: [IN] input - string to compress
 *  AUTHOR: Eliseev Dmitry
 ***/
VESTRING *VEStringDecomress( const VESTRING *input )
{
  VESTRING *processedString = NULL;

  /* Wrong argument */
  if (!input)
    return NULL;

  /* Wrong compressed string */
  if (input->m_Size <= 255)
    return NULL;

  /* That's it */
  return processedString;
} /* End of 'VEStringDecomress' function */
