#include "internalcontainer.h"
#include "internalstring.h"
#include "internalbuffer.h"
#include "memorymanager.h"
#include "string.h"

#include <string.h>

volatile static VEINTERNALCONTAINER *p_StringsContainer = NULL;

/***
 * PURPOSE: Initialize strings manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEStringInit( VEVOID )
{
  /* Already initialized */
  if (p_StringsContainer)
    return TRUE;

  p_StringsContainer = VEInternalContainerCreate(VE_DEFAULT_CONTAINERSIZE, "String container");
  if (!p_StringsContainer)
    return FALSE;
  return TRUE;
} /* End of 'p_StringsContainer' function */

/***
 * PURPOSE: Create empty string
 *  RETURN: Pointer to created string if success, NULL otherwise
 *   PARAM: [IN] size - string length
 *  AUTHOR: Eliseev Dmitry
 ***/
VESTRING* VEStringCreateInternal( const VEUINT size )
{
  VESTRING *newString = NULL;

  /* String creation */
  newString = New(sizeof(VESTRING), "String");
  if (!newString)
    return NULL;

  if (size > 0)
  {
    newString->m_Size = size;
    newString->m_Data = New(size, "String data");
    if (!newString->m_Data)
    {
      Delete(newString);
      return NULL;
    }
  }

  return newString;
} /* End of 'VEStringCreateInternal' function */

/***
 * PURPOSE: Create string from buffer
 *  RETURN: Pointer to created string if success, NULL otherwise
 *   PARAM: [IN] buffer - buffer
 *  AUTHOR: Eliseev Dmitry
 ***/
VESTRING* VEStringCreateFromBufferInternal( const VEBUFFER buffer )
{
  VEUINT bufLen = VEBufferLength(buffer);
  VESTRING *str = NULL;

  /* Wrong argument */
  if (bufLen == 0)
    return NULL;

  /* Allocate memory */
  str = VEStringCreateInternal(bufLen + 1);
  if (!str)
    return 0;

  /* Storing data */
  memcpy(str->m_Data, buffer, bufLen);
  str->m_Length = bufLen;

  /* That's it */
  return str;
} /* End of 'VEStringCreateFromBufferInternal' function */

/***
 * PURPOSE: Create empty string
 *  RETURN: Created string identifier if success, 0 otherwise
 *   PARAM: [IN] size - string length
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStringCreate( const VEUINT size )
{
  VESTRING *newString = NULL;

  /* Module is not initialized */
  if (!p_StringsContainer)
    return 0;

  newString = VEStringCreateInternal(size);
  if (!newString)
    return 0;

  return VEInternalContainerAdd((VEINTERNALCONTAINER*)p_StringsContainer, newString);
} /* End of 'VEStringCreate' function */

/***
 * PURPOSE: Create string using C-style buffer
 *  RETURN: Created string identifier if success, 0 otherwise
 *   PARAM: [IN] sourceString - C-style string representation
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStringCreateFromString( const VEBUFFER sourceString )
{
  VESTRING *newString = NULL;
  VEUINT sourceStringLength = 0;

  /* Module is not initialized */
  if (!p_StringsContainer)
    return 0;

  /* Determine source string length */
  while ((sourceString[sourceStringLength] != 0)&&(sourceString[sourceStringLength] != '\r')&&(sourceString[sourceStringLength] != '\n'))
    sourceStringLength++;
  if (sourceStringLength == 0)
    return 0;

  /* String creation */
  newString = VEStringCreateInternal(sourceStringLength+1);
  memcpy(newString->m_Data, sourceString, sourceStringLength);
  newString->m_Length = sourceStringLength;

  return VEInternalContainerAdd((VEINTERNALCONTAINER*)p_StringsContainer, newString);
} /* End of '' function */

VESTRING *VEStringCopyInternal( const VESTRING *sourceString )
{
  VESTRING *copiedString = VEStringCreateInternal(sourceString->m_Size);
  if (!copiedString)
    return NULL;

  /* Copy data */
  memcpy(copiedString->m_Data, sourceString->m_Data, sourceString->m_Size);
  copiedString->m_Length = sourceString->m_Length;

  return copiedString;
} /* End of 'VEStringCopyInternal' function */

/***
 * PURPOSE: Create a copy of string
 *  RETURN: Copied string identifier if success, 0 otherwise
 *   PARAM: [IN] stringID - source string identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStringCopy( const VEUINT stringID )
{
  /* Module is not initialized */
  if (!p_StringsContainer)
    return 0;
  return VEInternalContainerCopy((VEINTERNALCONTAINER*)p_StringsContainer, stringID, (VEFUNCTIONWITHRESULT)VEStringCopyInternal);
} /* End of 'VEStringCopy' function */

/***
 * PURPOSE: Delete existing string
 *   PARAM: [IN] stringPtr - pointer to string structure
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStringDeleteInternal( VESTRING *stringPtr )
{
  if (stringPtr)
    Delete(stringPtr->m_Data);
  Delete(stringPtr);
} /* End of 'VEStringDeleteInternal' function */

/***
 * PURPOSE: Delete existing string
 *   PARAM: [IN] stringID - identifier of string to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStringDelete( const VEUINT stringID )
{
  VESTRING *str = NULL;
  if (!p_StringsContainer)
    return;

  str = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StringsContainer, stringID);
  VEInternalContainerRemove((VEINTERNALCONTAINER*)p_StringsContainer, stringID);

  VEStringDeleteInternal(str);
} /* End of 'VEStringDelete' function */

/***
 * PURPOSE: Obtain C-style pointer to data buffer
 *  RETURN: Pointer to data buffer if success, NULL otherwise
 *   PARAM: [IN] stringID - string identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
const VEBUFFER VEStringPtr( const VEUINT stringID )
{
  VESTRING *str = NULL;
  if (!p_StringsContainer)
    return NULL;

  str = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StringsContainer, stringID);
  if (str)
    return str->m_Data;
  return NULL;
} /* End of 'VEStringPtr' function */

/***
 * PURPOSE: Determine string length
 *  RETURN: Length of string
 *   PARAM: [IN] stringID - string identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStringLength( const VEUINT stringID )
{
  VESTRING *str = NULL;
  if (!p_StringsContainer)
    return 0;

  str = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StringsContainer, stringID);
  if (str)
    return str->m_Length;
  return 0;
} /* End of 'VEStringLength' function */

/***
 * PURPOSE: Add two strings
 *  RETURN: Identifier of newly created string, which is concatenation of the others if success, 0 otherwise
 *   PARAM: [IN] leftStringID  - first string identifier
 *   PARAM: [IN] rightStringID - second string identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEStringAdd( const VEUINT leftStringID, const VEUINT rightStringID )
{
  VESTRING *leftString = NULL, *rightString = NULL, *newString = NULL;
  if (!p_StringsContainer)
    return 0;

  leftString  = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StringsContainer, leftStringID);
  rightString = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StringsContainer, rightStringID);

  if (!(leftString&&rightString))
    return 0;

  newString = VEStringCreateInternal(leftString->m_Length + rightString->m_Length + 1);
  memcpy(newString->m_Data, leftString->m_Data, leftString->m_Length);
  memcpy(&newString->m_Data[leftString->m_Length], rightString->m_Data, rightString->m_Length);
  newString->m_Length = leftString->m_Length + rightString->m_Length;

  return VEInternalContainerAdd((VEINTERNALCONTAINER*)p_StringsContainer, newString);
} /* End of 'VEStringAdd' function */

/***
 * PURPOSE: Erase string
 *   PARAM: [IN] stringID - string identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStringClear( const VEUINT stringID )
{
  VESTRING *str = NULL;
  if (!p_StringsContainer)
    return;

  str = VEInternalContainerGet((VEINTERNALCONTAINER*)p_StringsContainer, stringID);
  if (str)
  {
    memset(str->m_Data, 0, str->m_Size);
    str->m_Length = 0;
  }
} /* End of 'VEStringClear' function */

/***
 * PURPOSE: Initialize strings manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStringDeinit( VEVOID )
{
  if (p_StringsContainer)
  {
    VEInternalContainerForeach((VEINTERNALCONTAINER*)p_StringsContainer, (VEFUNCTION)VEStringDeleteInternal);
    VEInternalContainerDelete((VEINTERNALCONTAINER*)p_StringsContainer);
  }

  p_StringsContainer = NULL;
} /* End of 'VEStringDeinit' function */
