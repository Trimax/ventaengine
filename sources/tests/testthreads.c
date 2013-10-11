#ifdef _WIN32
#include <conio.h>
#endif

#include "tests.h"

VEVOID MyFunction1( VEPOINTER data )
{
  VEUINT counter = 10000;
  while (counter > 0)
  {
    VESectionEnter(1);
    printf("Function 1\n");
    VESectionLeave(1);

    counter--;
  }
}

VEVOID MyFunction2( VEPOINTER data )
{
  VEUINT counter = 10000;
  while (counter > 0)
  {
    VESectionEnter(1);
    printf("Function 2\n");
    VESectionLeave(1);

    counter--;
  }
}

/***
 * PURPOSE: Threads manager test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestThreads( VEVOID )
{
  VEUINT thread1 = VEThreadCreate(MyFunction1, NULL);
  VEUINT thread2 = VEThreadCreate(MyFunction2, NULL);

  VEThreadJoin(thread1);
  VEThreadJoin(thread2);
} /* End of 'TestMemory' function */
