#include "tests.h"

#include <stdio.h>

/***
 * PURPOSE: Strings manager test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestStrings( VEVOID )
{
  VEUINT strOriginalID = VEStringCreateFromString("Hello, world");
  VEUINT strCopyID     = VEStringCopy(strOriginalID);
  VEUINT strConcatID   = VEStringAdd(strOriginalID, strCopyID);

  printf("      Original: %s\n", VEStringPtr(strOriginalID));
  printf(" String length: %d\n", VEStringLength(strOriginalID));
  printf("\n");
  printf("        Copied: %s\n", VEStringPtr(strCopyID));
  printf(" String length: %d\n", VEStringLength(strCopyID));
  printf("\n");
  printf("  Concatenated: %s\n", VEStringPtr(strConcatID));
  printf(" String length: %d\n", VEStringLength(strConcatID));
  printf("\n");
  VEStringClear(strConcatID);
  printf("Cleared string: %s\n", VEStringPtr(strConcatID));
  printf(" String length: %d\n", VEStringLength(strConcatID));

  VEStringDelete(strConcatID);
} /* End of 'TestStrings' function */
