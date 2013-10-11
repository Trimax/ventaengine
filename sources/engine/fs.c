#include "memorymanager.h"
#include "internalfs.h"
#include "fs.h"

/***
 * PURPOSE: Open binary file to read
 *  RETURN: Pointer to file structure if success, NULL otherwise
 *   PARAM: [IN] name - file to open
 *  AUTHOR: Eliseev Dmitry
 ***/
VEFILE *VEFileOpenInternal( const VEBUFFER name )
{
  VEFILE *file = NULL;

  /* Wrong argument */
  if (!name)
    return NULL;

  /* Memory allocation */
  file = New(sizeof(VEFILE), "File");
  if (!file)
    return NULL;

  /* Store file name */
  file->m_Name = VEStringCreateFromBufferInternal(name);
  if (!file->m_Name)
  {
    VEFileCloseInternal(file);
    return NULL;
  }

  file->m_Ptr = fopen(name, "rb");
  if (!file->m_Ptr)
  {
    VEFileCloseInternal(file);
    return NULL;
  }

  /* Determine file size */
  fseek(file->m_Ptr, 0, SEEK_END);
  file->m_Size = ftell(file->m_Ptr);
  fseek(file->m_Ptr, 0, SEEK_SET);

  /* That's it */
  return file;
} /* End of 'VEFileOpenInternal' function */

/***
 * PURPOSE: Close binary file
 *   PARAM: [IN] file - pointer to file to close
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEFileCloseInternal( VEFILE *file )
{
  if (!file)
    return;

  /* Close file */
  if (file->m_Ptr)
    fclose(file->m_Ptr);

  VEStringDeleteInternal(file->m_Name);
  Delete(file);
} /* End of 'VEFileCloseInternal' function */
