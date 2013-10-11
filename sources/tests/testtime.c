#include <stdio.h>

#include "tests.h"

/***
 * PURPOSE: Test time
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestTime( VEVOID )
{
  VEINT i = 0;

  for (i = 0; i < 1024; i++)
  {
    printf("Now: %ld\n", VETime());
    VEWait(10);
  }
} /* End of 'TestTime' function */
