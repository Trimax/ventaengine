#include "tests.h"

#include <stdio.h>

/***
 * PURPOSE: Strings manager test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestDatabase( VEVOID )
{
  VEUINT connectionID = VEDatabaseConnect("localhost", 3306, "root", "root", "test");
  printf("Connection established. Identifier = %d\n", connectionID);

  VEDatabaseDisconnect(connectionID);
} /* End of 'TestDatabase' function */
