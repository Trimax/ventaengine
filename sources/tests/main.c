#include "tests.h"

volatile static VEBOOL p_IsGameActive = TRUE;

/***
 * PURPOSE: Keyboard processor
 *   PARAM: [IN] key - pressed key
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID KeyboardProcessor( VEKEY key )
{
  VECHAR test[VE_BUFFER_SMALL];

  sprintf(test, "Key: %d; CTRL: %c; ALT: %c; SHIFT: %c\n", key.m_Code, key.m_Ctrl, key.m_Alt, key.m_Shift);
  printf("%s\n", test);
  VEConsolePrint(test);

  if (key.m_Code == VE_KEY_ESCAPE)
  {
    char cmd[] = "quit";
    p_IsGameActive = FALSE;
    VEConsoleProcess(cmd);
  }
} /* End of 'KeyboardProcessor' function */

/***
 * PURPOSE: Initialize project data
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID Constructor( VEVOID )
{
  VEConsolePrint("Test projects initialized");
  VELoggerInfo("Test projects initialized");
} /* End of 'Constucor' function */

/***
 * PURPOSE: Deinitialize project data
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID Destructor( VEVOID )
{
  VEConsolePrint("Test projects deinitialized");
  VELoggerInfo("Test projects deinitialized");
} /* End of 'Constucor' function */

VEVOID SomeGameLogic( VEPOINTER data )
{
  while (VEIsRunning())
  {
    /* Move camera along Z */
    if (VEIsKeyPressed(VE_KEY_UP))
      printf("yeah\n");


    VEWait(10);
  }

  printf("Shutting down game...\n");
} /* End of 'SomeGameLogic' function */

/***
 * PURPOSE: Main program function
 *  RETURN: Always 0
 *   PARAM: [IN] argc - the number of CL parameters
 *   PARAM: [IN] argv - CL parameters
 *  AUTHOR: Eliseev Dmitry
 ***/
VEINT main( VEINT argc, VEBUFFER argv[] )
{
  VECONFIGURATION cfg = VEGetConfigurationDefault();
  cfg.m_EnableLogger = TRUE;
  cfg.m_EnableDatabaseSystem = TRUE;
  //cfg.m_EnableVideoSystem = TRUE;
  cfg.m_KeyboardProcessor = KeyboardProcessor;
  cfg.m_Constructor = Constructor;
  cfg.m_Destructor  = Destructor;
  cfg.m_ArgsCount = argc;
  cfg.m_Arguments = argv;
  //cfg.m_EnableFullScreen = TRUE;

  cfg.m_EnableShaders = TRUE;

  VEConfigure(&cfg);

  /* Initialize Venta engine */
  VEInit();

  /* Test argumets */
  printf("Argument 'arg1' defined: %d\n", VEIsArgumentDefined("arg1"));
  printf("Argument 'arg2' defined: %d\n", VEIsArgumentDefined("arg2"));
  printf("Argument 'arg3' defined: %d\n", VEIsArgumentDefined("arg3"));
  printf("Argument 'flag' defined: %d\n", VEIsArgumentDefined("flag"));
  printf("Argument 'test' defined: %d\n", VEIsArgumentDefined("test"));

  if (VEIsArgumentDefined("arg2"))
    printf("Argument 'arg2' value = %s\n", VEArgumentGetValue("arg2"));

  /* Start test thread */
  //VEThreadCreate(SomeGameLogic, NULL);
  //TestMemory();
  //TestThreads();
  //TestPool();
  //TestStrings();
  //TestDatabase();
  //TestStructures();
  //TestQueueStack();
  //TestTime();
  //TestGraphics();

  TestMath();

  //VERun();

  /* Deinitialize Venta engine */
  VEDeinit();
  return 0;
} /* End of 'main' function */
