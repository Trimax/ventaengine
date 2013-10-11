#include "internalheader.h"

/* Valid header preffix */
#define VE_HEADER_VALID 'V'

/***
 * PURPOSE: Check if header is valid
 *  RETURN: TRUE if success, FALSE otherwise
 *   PARAM: [IN] header     - header to validate
 *   PARAM: [IN] fileFormat - expected file format (see VE_FORMAT_XXX definitions)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEHeaderIsValid( const VEHEADER header, const VEBYTE fileFormat )
{
  return (header.m_Prefix == VE_HEADER_VALID)&&(header.m_Version == VE_FORMATVERSION)&&(header.m_Format == fileFormat);
} /* End of 'VEHeaderIsValid' function */
