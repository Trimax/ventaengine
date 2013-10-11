#ifndef TESTS_H_INCLUDED
#define TESTS_H_INCLUDED

#include <stdio.h>

#include "engine/engine.h"

/***
 * PURPOSE: Memory manager test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestMemory( VEVOID );

/***
 * PURPOSE: Threads manager test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestThreads( VEVOID );

/***
 * PURPOSE: Pool manager test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestPool( VEVOID );

/***
 * PURPOSE: Strings manager test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestStrings( VEVOID );

/***
 * PURPOSE: Strings manager test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestDatabase( VEVOID );

/***
 * PURPOSE: Structures manager test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestStructures( VEVOID );

/***
 * PURPOSE: Queue & stack test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestQueueStack( VEVOID );

/***
 * PURPOSE: Test time
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestTime( VEVOID );

/***
 * PURPOSE: Graphics test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestGraphics( VEVOID );

/***
 * PURPOSE: Math test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestMath( VEVOID );

#endif // TESTS_H_INCLUDED
