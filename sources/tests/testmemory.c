#include "tests.h"

/***
 * PURPOSE: Memory manager test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestMemory( VEVOID )
{
  VEUINT variableID = 0;

  printf("Starting memory test...");
  for (variableID = 0; variableID < 2048; variableID++)
    New(sizeof(int) * variableID, NULL);
  printf("SUCCESS\n");

  printf("Memory used: %ld bytes\n", MemoryUsed());
} /* End of 'TestMemory' function */
