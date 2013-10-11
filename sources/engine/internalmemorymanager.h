#ifndef MEMORY_H_INCLUDED
#define MEMORY_H_INCLUDED

#include "types.h"

/* Small comment size */
#define VE_COMMENT_SMALL 256

/* Memory block structure definition */
typedef struct tagMEMORYBLOCK
{
  VEULONG   m_Size;                      /* Allocated memory block size */
  VEPOINTER m_Ptr;                       /* Pointer to memory block start */
  VECHAR    m_Comment[VE_COMMENT_SMALL]; /* Allocation comment */

  struct tagMEMORYBLOCK *m_Next;         /* Next memory block */
} VEMEMORYBLOCK;

/***
 * PURPOSE: Initialize memory manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEMemoryInit( VEVOID );

/***
 * PURPOSE: Deinitialize memory manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEMemoryDeinit( VEVOID );

#endif // MEMORY_H_INCLUDED
