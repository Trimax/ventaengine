#include "internaldialogmanager.h"
#include "internalbuffer.h"
#include "internalstring.h"
#include "memorymanager.h"
#include "string.h"

#ifndef _WIN32
#include <strings.h>
#endif

#include <string.h>
#include <stdlib.h>

/***
 * PURPOSE: Determine message length
 *  RETURN: Message length
 *   PARAM: [IN] message - message to determine length
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEBufferLength( const VEBUFFER message )
{
  VEUINT pos = 0;
  if (!message)
    return 0;

  while ((message[pos] != 0)&&(message[pos] != '\r')&&(message[pos] != '\n'))
    pos++;

  return pos;
} /* End of 'VEBufferLength' function */

/***
 * PURPOSE: Squeeze symbols at buffer
 *  RETURN: Message length
 *   PARAM: [IN] message - message to determine length
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBufferSqueeze( VEBUFFER buffer, const VECHAR symbol )
{
  VEUINT length = VEBufferLength(buffer), offset = 0, pos = 0;
  VEBOOL isFirstSymbol = TRUE;


  while (pos < length)
  {
    if (buffer[pos] == symbol)
    {
      if (!isFirstSymbol)
        offset++;
      else
        isFirstSymbol = FALSE;
    } else
      isFirstSymbol = TRUE;

    buffer[pos-offset] = buffer[pos];
    pos++;
  }

  buffer[pos-offset] = 0;
} /* End of 'VEBufferSqueeze' function */

/***
 * PURPOSE: Split buffer using separator
 *  RETURN: Pointer to splitted buffer as instance of an array of strings
 *   PARAM: [IN] src       - source buffer to split
 *   PARAM: [IN] separator - character, that separates values
 *  AUTHOR: Eliseev Dmitry
 ***/
VEARRAY *VEBufferSplit( const VEBUFFER src, const VECHAR separator )
{
  VEARRAY *array = NULL;
  VEUINT numItems = 1, pos = 0, length = VEBufferLength(src), lastPos = 0, itemID = 0;

  /* Remove multiple separators */
  VEBufferSqueeze(src, separator);

  /* Wrong argument */
  if (!src)
    return NULL;

  /* Determine number of items */
  for (pos = 0; pos < length; pos++)
    if (src[pos] == separator)
      numItems++;

  /* Array creation */
  array = VEArrayCreateInternal(numItems);
  if (!array)
    return NULL;

  /* Store the number of items in array */
  array->m_NumItems = numItems;

  /* Split source buffer and store result in array */
  pos = 0;
  while (pos < length)
  {
    /* Next separator found */
    if (src[pos] == separator)
    {
      VEUINT itemLength = pos - lastPos;

      /* Not empty item */
      if (itemLength > 0)
      {
        array->m_Items[itemID] = (VEPOINTER)VEStringCreateInternal(itemLength+1);
        memcpy(((VESTRING*)array->m_Items[itemID])->m_Data, &src[lastPos], itemLength);
      }

      /* Next item */
      itemID++;
      lastPos = pos+1;
    }

    pos++;
  }

  /* Last argument */
  if (lastPos != pos)
  {
    array->m_Items[itemID] = (VEPOINTER)VEStringCreateInternal(pos-lastPos+1);
    memcpy(((VESTRING*)array->m_Items[itemID])->m_Data, &src[lastPos], pos-lastPos);
  }

  /* That's it */
  return array;
} /* End of 'VEBufferSplit' function */

/***
 * PURPOSE: Trim symbols from the begining and ending of string
 *   PARAM: [IN] message - message to trim
 *   PARAM: [IN] symbol  - symbol to trim
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBufferTrim( VEBUFFER message, const VECHAR symbol )
{
  VEINT length = VEBufferLength(message), pos = 0, offset = 0;

  /* Wrong arguments */
  if (length == 0)
    return;

  /* Trim symbols from left side */
  for (pos = 0; (pos < length)&&(message[pos] == symbol); pos++);
  if (pos > 0)
  {
    offset = pos;
    for (pos = offset; pos < length; pos++)
      message[pos-offset] = message[pos];
    memset(&message[pos-offset], 0, offset);
  }

  /* Update length */
  length = VEBufferLength(message);
  if (length == 0)
    return;

  /* Trim symbols from right side */
  for (pos = length-1; (pos > 0)&&(message[pos] == symbol); pos--)
    message[pos] = 0;
} /* End of 'VEBufferTrim' function */

/***
 * PURPOSE: Compare two buffers
 *  RETURN: Difference between two strings
 *   PARAM: [IN] src1 - first string
 *   PARAM: [IN] src2 - second string
 *  AUTHOR: Eliseev Dmitry
 ***/
VEINT VEBuffersDiff( const VEBUFFER src1, const VEBUFFER src2 )
{
  #ifdef _WIN32
  return strcmpi(src1, src2);
  #else
  return strcasecmp(src1, src2);
  #endif
} /* End of 'VEBuffersDiff' function */

/***
 * PURPOSE: Compare two buffers
 *  RETURN: TRUE if buffers are the same, FALSE otherwise
 *   PARAM: [IN] src1 - first string
 *   PARAM: [IN] src2 - second string
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEBuffersEqual( const VEBUFFER src1, const VEBUFFER src2 )
{
  return (VEBuffersDiff(src1, src2) == 0);
} /* End of 'VEBuffersEqual' function */

/***
 * PURPOSE: Remove symbols from the string
 *   PARAM: [IN] message - message to trim
 *   PARAM: [IN] symbol  - symbol to trim
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBufferRemoveAll( VEBUFFER message, const VECHAR symbol )
{
  VEINT pos = 0, offset = 0, length = VEBufferLength(message);

  if (!message)
    return;

  /* Replace all characters */
  for (pos = 0; pos < length; pos++)
    if (message[pos] == symbol)
      offset++;
    else
      message[pos-offset] = message[pos];
  message[pos-offset] = 0;
} /* End of 'VEBufferRemoveAll' function */

/***
 * PURPOSE: Compare end of buffer with sample
 *  RETURN: TRUE if end of buffer is the same as sample, FALSE otherwise
 *   PARAM: [IN] src    - source buffer to check
 *   PARAM: [IN] sample - buffer to look up
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEBufferIsEndsWith( const VEBUFFER src, const VEBUFFER sample )
{
  /* Determine lengths */
  VEUINT sampleLen = VEBufferLength(sample);
  VEUINT bufferLen = VEBufferLength(src);

  /* Wrong pointers */
  if (!(src&&sample))
    return FALSE;

  /* Sample is greater that source */
  if (sampleLen > bufferLen)
    return FALSE;

  /* Buffers are the same length */
  if (sampleLen == bufferLen)
    return VEBuffersEqual(src, sample);

  /* Compare substrings */
  return VEBuffersEqual(&src[bufferLen - sampleLen], sample);
} /* End of 'VEBufferIsEndsWith' function */

/***
 * PURPOSE: Replace all symbol occurrences with another one
 *   PARAM: [IN] message   - message to process
 *   PARAM: [IN] symbol    - symbol to replace
 *   PARAM: [IN] newSymbol - new symbol
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBufferReplaceAll( VEBUFFER message, const VECHAR symbol, const VECHAR newSymbol )
{
  VEUINT pos = 0, len = VEBufferLength(message);

  if (!message)
    return;

  /* Replace symbols */
  for (pos = 0; pos < len; pos++)
    if (message[pos] == symbol)
      message[pos] = newSymbol;
} /* End of 'VEBufferReplaceAll' function */

/***
 * PURPOSE: Find position of last occurrence of symbol at string
 *  RETURN: Last symbol's occurrence position if success, -1 otherwise
 *   PARAM: [IN] src    - source buffer to check
 *   PARAM: [IN] symbol - symbol to find position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEINT VEBufferLastIndexOf( const VEBUFFER src, const VECHAR symbol )
{
  VEINT pos = 0, len = VEBufferLength(src);
  if (!src)
    return -1;

  for (pos = len; pos > 0; pos--)
    if (src[pos] == symbol)
      return pos;
  return -1;
} /* End of 'VEBufferLastIndexOf' function */

/***
 * PURPOSE: Find position of first occurrence of symbol at string starting from position
 *  RETURN: First symbol's occurrence position if success, -1 otherwise
 *   PARAM: [IN] src    - source buffer to check
 *   PARAM: [IN] from   - startup position
 *   PARAM: [IN] symbol - symbol to find position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEINT VEBufferIndexOf( const VEBUFFER src, const VEUINT from, const VECHAR symbol )
{
  VEINT pos = 0, len = VEBufferLength(src);
  if (!src)
    return -1;

  for (pos = from; pos < len; pos++)
    if (src[pos] == symbol)
      return pos;
  return -1;
} /* End of 'VEBufferIndexOf' function */

/***
 * PURPOSE: Find position of first occurrence of symbol at string
 *  RETURN: First symbol's occurrence position if success, -1 otherwise
 *   PARAM: [IN] src    - source buffer to check
 *   PARAM: [IN] symbol - symbol to find position
 *  AUTHOR: Eliseev Dmitry
 ***/
VEINT VEBufferFirstIndexOf( const VEBUFFER src, const VECHAR symbol )
{
  return VEBufferIndexOf(src, 0, symbol);
} /* End of 'VEBufferFirstIndexOf' function */

/***
 * PURPOSE: Convert buffer to an integer
 *  RETURN: Buffer's integer representation if success, 0 otherwise
 *   PARAM: [IN] src - source buffer to convert
 *  AUTHOR: Eliseev Dmitry
 ***/
VEINT VEBufferToInt( const VEBUFFER src )
{
  if (!src)
    return 0;

  return atoi(src);
} /* End of 'VEBufferToInt' function */

/***
 * PURPOSE: Convert buffer to a real
 *  RETURN: Buffer's real representation if success, 0 otherwise
 *   PARAM: [IN] src - source buffer to convert
 *  AUTHOR: Eliseev Dmitry
 ***/
VEREAL VEBufferToReal( const VEBUFFER src )
{
  if (!src)
    return 0.0;

  return atof(src);
} /* End of 'VEBufferToReal' function */

/***
 * PURPOSE: Convert buffer to a color
 *  RETURN: Buffer's color representation if success, 0 otherwise
 *   PARAM: [IN] src - source buffer to convert
 *  AUTHOR: Eliseev Dmitry
 ***/
VECOLOR VEBufferToColor( const VEBUFFER src )
{
  VEUINT numSeparators = 0, pos = 0;
  VEUINT length = VEBufferLength(src);
  VECOLOR color = VECOLOR_BLACK;

  /* Wrong source buffer */
  if (!src)
    return VECOLOR_BLACK;

  /* Too short color [0,0,0,0] */
  if (length < 9)
    return VECOLOR_BLACK;

  /* Too long color [255,255,255,255] */
  if (length > 17)
    return VECOLOR_BLACK;

  /* Wrong array */
  if ((src[0] != VE_SYMBOL_ARRAYBEGIN)||(src[length-1] != VE_SYMBOL_ARRAYEND))
    return VECOLOR_BLACK;

  /* Calculate the number of separators */
  for (pos = 0; pos < length; pos++)
    if (src[pos] == VE_SYMBOL_SEPARATOR)
      numSeparators++;

  /* Wrong number of components */
  if (numSeparators != 3)
    return VECOLOR_BLACK;

  {
    VECHAR channel[VE_BUFFER_SMALL];
    VEUINT channelLen = 0;

    VEUINT separator1Pos = VEBufferFirstIndexOf(src, VE_SYMBOL_SEPARATOR);
    VEUINT separator2Pos = VEBufferIndexOf(src, separator1Pos+1, VE_SYMBOL_SEPARATOR);
    VEUINT separator3Pos = VEBufferIndexOf(src, separator2Pos+1, VE_SYMBOL_SEPARATOR);

    /* Red channel extraction */
    channelLen = separator1Pos - 1;
    if ((channelLen == 0)||(channelLen > 3))
      return VECOLOR_BLACK;
    memset(channel, 0, VE_BUFFER_SMALL);
    memcpy(channel, &src[1], channelLen);
    color.m_Red = VEBufferToInt(channel);

    /* Green channel extraction */
    channelLen = separator2Pos - separator1Pos - 1;
    if ((channelLen == 0)||(channelLen > 3))
      return VECOLOR_BLACK;
    memset(channel, 0, VE_BUFFER_SMALL);
    memcpy(channel, &src[separator1Pos+1], channelLen);
    color.m_Green = VEBufferToInt(channel);

    /* Blue channel extraction */
    channelLen = separator3Pos - separator2Pos - 1;
    if ((channelLen == 0)||(channelLen > 3))
      return VECOLOR_BLACK;
    memset(channel, 0, VE_BUFFER_SMALL);
    memcpy(channel, &src[separator2Pos+1], channelLen);
    color.m_Blue = VEBufferToInt(channel);

    /* Alpha channel extraction */
    channelLen = length - separator3Pos - 2;
    if ((channelLen == 0)||(channelLen > 3))
      return VECOLOR_BLACK;
    memset(channel, 0, VE_BUFFER_SMALL);
    memcpy(channel, &src[separator3Pos+1], channelLen);
    color.m_Alpha = VEBufferToInt(channel);
  }

  /* That's it */
  return color;
} /* End of 'VEBufferToColor' function */

/***
 * PURPOSE: Write buffer to output stream
 *   PARAM: [IN] out - opened output stream
 *   PARAM: [IN] buf - buffer to write
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEBufferWrite( FILE *out, const VEBUFFER buf )
{
  VEUINT length = VEBufferLength(buf);

  /* Wrong pointers */
  if (!(out&&buf))
    return;

  /* Write data */
  fwrite(&length, 1, sizeof(VEUINT), out);
  fwrite(buf, 1, length, out);
} /* End of 'VEBufferWrite' function */

/***
 * PURPOSE: Read buffer from input stream
 *  RETURN: Readed buffer if success, NULL otherwise
 *   PARAM: [IN] in - opened input stream
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBUFFER VEBufferRead( FILE *in )
{
  VEBUFFER data = NULL;
  VEUINT bufSize = 0;

  /* Wrong pointer */
  if (!in)
    return NULL;

  /* Reading buffer size */
  fread(&bufSize, 1, sizeof(VEUINT), in);

  /* Memory allocation */
  data = New(bufSize+1, "Readed buffer");
  if (!data)
    return NULL;

  /* Reading buffer */
  fread(data, 1, bufSize, in);
  return data;
} /* End of 'VEBufferRead' function */
