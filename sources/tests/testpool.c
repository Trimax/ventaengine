#ifdef _WIN32
#include <conio.h>
#endif

#include <stdlib.h>

#include "tests.h"

VEVOID Task1( const VEPOINTER ptr )
{
  VEUINT counter = 1000;

  while (counter > 0)
  {
    VESectionEnter(1);
    printf("Strong calculations (task1): %d\n", counter);
    VESectionLeave(1);
    VEWait(10);
    counter--;
  }
}

VEVOID Task2( const VEPOINTER ptr )
{
  VEUINT counter = 1000;

  while (counter > 0)
  {
    VESectionEnter(1);
    printf("Strong calculations (task 2): %d\n", counter);
    VESectionLeave(1);
    VEWait(10);
    counter--;
  }
}

VEVOID Task3( const VEPOINTER ptr )
{
  VEUINT counter = 1000;

  while (counter > 0)
  {
    VESectionEnter(1);
    printf("Strong calculations (task 3): %d\n", counter);
    VESectionLeave(1);
    VEWait(10);
    counter--;
  }
}

/***
 * PURPOSE: Pool manager test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestPool( VEVOID )
{
  VEUINT poolID = VEPoolCreate(4);
  VEUINT taskID = 0;

  for (taskID = 0; taskID < 10; taskID++)
  {
    VEINT taskNumber = rand()%3;

    if (taskNumber == 0)
      VEPoolPush(poolID, Task1, NULL);
    else if (taskNumber == 1)
      VEPoolPush(poolID, Task2, NULL);
    else if (taskNumber == 2)
      VEPoolPush(poolID, Task3, NULL);
  }

  VEWait(10000);
  VEPoolDelete(poolID);
} /* End of 'TestPool' function */
