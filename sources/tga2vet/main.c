#include "engine/internaltexturemanager.h"
#include "engine/internalbuffer.h"
#include "engine/engine.h"

#include <stdio.h>
#include <stdlib.h>

/* Channels swapping flag */
VEBOOL swapFlag = FALSE, compressFlag = FALSE;

/***
 * PURPOSE: Main program function
 *  RETURN: Always 0
 *   PARAM: [IN] argc - the number of CL parameters
 *   PARAM: [IN] argv - CL parameters
 *  AUTHOR: Eliseev Dmitry
 ***/
int main( int argc, char *argv[] )
{
  FILE *in = NULL;
  VEBUFFER inputFile = NULL, outputFile = NULL;
  VETEXTURE *texture = NULL;
  VECONFIGURATION cfg = VEGetConfigurationDefault();
  cfg.m_EnableLogger = FALSE;
  cfg.m_ArgsCount = argc;
  cfg.m_Arguments = argv;

    /* Configure engine */
  VEConfigure(&cfg);

  /* Initialize Venta engine */
  VEInit();

  /* Is it help screen? */
  if (VEIsArgumentDefined("help"))
  {
    printf("Use: tga2vet [-swap] [-compress] -input <sourceFile> -output <destinationFile>\n\n");
    printf("        swap - flag enforce utility to swap RED & BLUE channels\n");
    printf("    compress - flag enforce utility to compress converted file\n");
    printf("       input - source file (TGA format)\n");
    printf("      output - converted file (VET format)\n");
    VEDeinit();
    exit(0);
  }

  /* Swap channels flag */
  swapFlag = VEIsArgumentDefined("swap");

  /* Compress texture flag */
  compressFlag = VEIsArgumentDefined("compress");

  /* Input & output files */
  inputFile = VEArgumentGetValue("input");
  outputFile = VEArgumentGetValue("output");

  /* Check the number of arguments */
  if (!(inputFile && outputFile))
  {
    printf("There are not enough of parameters. See tga2vet -help\n");
    VEDeinit();
    exit(0);
  }

  /* Open input file */
  in = fopen(inputFile, "rb");
  if (!in)
  {
    printf("File %s not found\n", inputFile);
    VEDeinit();
    exit(0);
  }

  /* Status message */
  printf("Converting %s to %s...", inputFile, outputFile);

  /* Load TGA texture & close input file */
  texture = VETextureLoadTGA(in);
  fclose(in);

  /* Is file corrupted? */
  if (!texture)
  {
    printf("FAILED\nWrong input file format\n");
    VEDeinit();
    exit(0);
  }

  /* Do we need to swap channels? */
  if (swapFlag)
  {
    VEUINT i = 0;
    VEBYTE temp = 0;

    for (i = 0; i < texture->m_Width * texture->m_Height; i++)
    {
      temp = texture->m_Data[4 * i];
      texture->m_Data[4 * i] = texture->m_Data[4 * i + 2];
      texture->m_Data[4 * i + 2] = temp;
    }
  }

  /* Store texture as VET file */
  VETextureSave(outputFile, texture, compressFlag);
  printf("OK");

  /* Deinitialize Venta engine */
  VEDeinit();
  return 0;
} /* End of 'main' function */
