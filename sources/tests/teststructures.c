#include <stdio.h>

#include "tests.h"

#define VE_TESTS_NUMITEMS 1024

/***
 * PURPOSE: Processor function
 *   PARAM: [IN] data - pointer to user's data
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID Processor( VEPOINTER data )
{
  VEUINT number = *(VEUINT*)(data);
  printf("Number: %d\n", number);
} /* End of 'Processor function */

/***
 * PURPOSE: Structures manager test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestStructures( VEVOID )
{
  VEBYTE structure = VE_STRUCTURE_ARRAY;
  VEUINT localVariable = 3484609, i = 0;

  /* Create data structure */
  VEUINT structureID = VEStructureCreate(structure);
  printf("Size of structure: %d\n", VEStructureGetSize(structureID));

  printf("Adding items...\n");
  for (i = 0; i < VE_TESTS_NUMITEMS; i++)
    VEStructurePut(structureID, &localVariable);
  printf("Size of structure: %d\n", VEStructureGetSize(structureID));

  /* For each tester */
  VEStructureForeach(structureID, Processor);

  /* Remove some items from center */
  printf("Remove items...\n");
  for (i = VE_TESTS_NUMITEMS / 4; i < VE_TESTS_NUMITEMS; i++)
    VEStructureRemove(structureID, i);
  printf("Size of structure: %d\n", VEStructureGetSize(structureID));

  printf("Extract items...\n");
  for (i = VE_TESTS_NUMITEMS / 2; i < VE_TESTS_NUMITEMS; i++)
    VEStructureExtract(structureID, i);
  printf("Size of structure: %d\n", VEStructureGetSize(structureID));

  /* Remove data structure */
  VEStructureDelete(structureID);
} /* End of 'TestStructures' function */


/***
 * PURPOSE: Queue & stack test
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID TestQueueStack( VEVOID )
{
  VEBYTE structureID = VEStructureCreate(VE_STRUCTURE_STACK);
  VEUINT i = 0;

  /* Initialize local variables */
  VEUINT numbers[VE_TESTS_NUMITEMS];
  for (i = 0; i < VE_TESTS_NUMITEMS; i++)
    numbers[i] = i;

  /* Filling structure */
  printf("Adding items\n");
  for (i = 0; i < VE_TESTS_NUMITEMS; i++)
    VEStructurePut(structureID, &numbers[i]);
  printf("Size of structure: %d\n", VEStructureGetSize(structureID));

  /* Output structure */
  VEStructureForeach(structureID, Processor);

  /* Get items */
  for (i = 0; i < VE_TESTS_NUMITEMS; i++)
    VEStructureGet(structureID, 0);
  printf("Size of structure: %d\n", VEStructureGetSize(structureID));

  /* Extract items */
  for (i = 0; i < VE_TESTS_NUMITEMS; i++)
    VEStructureExtract(structureID, 0);
  printf("Size of structure: %d\n", VEStructureGetSize(structureID));

  /* Underflow imitation */
  for (i = 0; i < 10; i++)
    VEStructureRemove(structureID, 0);
  printf("Size of structure: %d\n", VEStructureGetSize(structureID));

  /* Remove structure */
  VEStructureDelete(structureID);
} /* End of 'TestQueueStack' function */
