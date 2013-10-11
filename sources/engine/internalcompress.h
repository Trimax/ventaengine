#ifndef INTERNALCOMPRESS_H_INCLUDED
#define INTERNALCOMPRESS_H_INCLUDED

#include "internalstring.h"

/***
 * PURPOSE: Compress string using Huffman's compression
 *  RETURN: Pointer to compressed string
 *   PARAM: [IN] input - string to compress
 *  AUTHOR: Eliseev Dmitry
 ***/
VESTRING *VEStringCompress( const VESTRING *input );

/***
 * PURPOSE: Decompress string using Huffman's decompression
 *  RETURN: Pointer to decompressed string
 *   PARAM: [IN] input - string to compress
 *  AUTHOR: Eliseev Dmitry
 ***/
VESTRING *VEStringDecomress( const VESTRING *input );

#endif // INTERNALCOMPRESS_H_INCLUDED
