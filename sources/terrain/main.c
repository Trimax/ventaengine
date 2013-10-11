#include "engine/internalterrain.h"
#include "engine/engine.h"

#include <stdlib.h>
#include <stdio.h>

/***
 * PURPOSE: Main program function
 *  RETURN: Always 0
 *   PARAM: [IN] argc - the number of CL parameters
 *   PARAM: [IN] argv - CL parameters
 *  AUTHOR: Eliseev Dmitry
 ***/
VEINT main( VEINT argc, VEBUFFER argv[] )
{
  VEBUFFER map1 = NULL, map2 = NULL, map3 = NULL, map4 = NULL, mapBH = NULL, outputFile = NULL;
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
    printf("Use: terrain -map1 <map1> -map2 <map2> -map3 <map3> -map4 <map4> -mapBH <blendHeightMap>\n\n");
    printf("        map1 - texture map unit #1\n");
    printf("        map2 - texture map unit #2\n");
    printf("        map3 - texture map unit #3\n");
    printf("        map4 - texture map unit #4\n");
    printf("       mapBH - blend map file\n");
    printf("      output - output terrain file\n");
    VEDeinit();
    exit(0);
  }

  /* Parse arguments */
  outputFile = VEArgumentGetValue("output");
  map1       = VEArgumentGetValue("map1");
  map2       = VEArgumentGetValue("map2");
  map3       = VEArgumentGetValue("map3");
  map4       = VEArgumentGetValue("map4");
  mapBH      = VEArgumentGetValue("mapBH");

  /* Check arguments */
  if (!(outputFile && map1 && map2 && map3 && map4 && mapBH))
  {
    printf("There are not enough of parameters. See terrain -help\n");
    VEDeinit();
    exit(0);
  }

  /* Try to create terrain file */
  printf("Terrain creation...");
  if (!VETerrainBuildInternal(mapBH, map1, map2, map3, map4, outputFile))
  {
    printf("FAILED\nEither blend map file was not found or it's corrupted\n");
    VEDeinit();
    exit(0);
  }

  /* Deinitialize Venta engine */
  printf("OK");
  VEDeinit();

  /* That's it */
  return 0;
} /* End of 'main' function */
