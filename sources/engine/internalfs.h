#ifndef INTERNALFS_H_INCLUDED
#define INTERNALFS_H_INCLUDED

#include "internalstring.h"

#include <stdio.h>

/* File structure */
typedef struct tagVEFILE
{
  VESTRING *m_Name;
  VEULONG   m_Size;
  FILE     *m_Ptr;
  VEBOOL    m_Type;
} VEFILE;

/***
 * PURPOSE: Open binary file to read
 *  RETURN: Pointer to file structure if success, NULL otherwise
 *   PARAM: [IN] name - file to open
 *  AUTHOR: Eliseev Dmitry
 ***/
VEFILE *VEFileOpenInternal( const VEBUFFER name );

/***
 * PURPOSE: Close binary file
 *   PARAM: [IN] file - pointer to file to close
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEFileCloseInternal( VEFILE *file );

#endif // INTERNALFS_H_INCLUDED
