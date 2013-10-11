#include "internalglut.h"

#include "internalstructuresmanager.h"
#include "internalinterfacemanager.h"
#include "internalcriticalsection.h"
#include "internalresourcemanager.h"
#include "internaltexturemanager.h"
#include "internalmemorymanager.h"
#include "internalscenemanager.h"
#include "internaltreemanager.h"
#include "internalarguments.h"
#include "internaldatabase.h"
#include "internalsettings.h"
#include "internalgraphics.h"
#include "internalconsole.h"
#include "internalstring.h"
#include "internallogger.h"
#include "configuration.h"
#include "internalinput.h"
#include "internalmenu.h"
#include "internaltask.h"
#include "internalcore.h"
#include "engine.h"

#include <stdlib.h>
#include <string.h>
#include <stdio.h>

/* Manually configured flag */
volatile static VEBOOL p_EngineManuallyConfigured = FALSE;

/* Engine configuration */
volatile static VECONFIGURATION p_EngineConfiguration;

/* Engine initialization flag */
volatile static VEBOOL p_EngineInitialized = FALSE;

/* Stop engine flag */
volatile static VEBOOL p_StopEngine = FALSE;

/*** Default configuration values ***/

/* Maximal number of threads */
#define VE_MAXIMAL_THREADS  1024

/* Maximal number of critical sections */
#define VE_MAXIMAL_SECTIONS 2048

/* Maximal pools number */
#define VE_MAXIMAL_POOLS 16

/***
 * PURPOSE: Get default configuration
 *  RETURN: Default configuration
 *  AUTHOR: Eliseev Dmitry
 ***/
VECONFIGURATION VEGetConfigurationDefault( VEVOID )
{
  VECONFIGURATION cfg;
  memset(&cfg, 0, sizeof(VECONFIGURATION));

  /* Memory manager configuration */
  cfg.m_EnableMemorySystem = TRUE;

  /* Default logging level */
  cfg.m_LoggingLevel = VE_LOGLEVEL_INFO;

  /* Multitasks configuration */
  cfg.m_MaximalThreadsNumber          = VE_MAXIMAL_THREADS;
  cfg.m_MaximalCriticalSectionsNumber = VE_MAXIMAL_SECTIONS;
  cfg.m_MaximalPoolsNumber            = VE_MAXIMAL_POOLS;

  /* Video subsystem configuration */
  cfg.m_VideoScreenWidth  = VE_DEFAULT_SCREENWIDTH;
  cfg.m_VideoScreenHeight = VE_DEFAULT_SCREENHEIGHT;
  cfg.m_EnableFullScreen  = FALSE;

  /* No command-line arguments by default */
  cfg.m_ArgsCount = 0;
  cfg.m_Arguments = NULL;

  return cfg;
} /* End of 'VEGetConfigurationDefault' function */

/***
 * PURPOSE: Set Venta engine configuration
 *   PARAM: [IN] configuration - pointer to filled configuration
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConfigure( const VECONFIGURATION *configuration )
{
  memcpy((VEPOINTER)&p_EngineConfiguration, configuration, sizeof(VECONFIGURATION));
  p_EngineManuallyConfigured = TRUE;
} /* End of 'VEConfigure' function */

/***
 * PURPOSE: Initialize Venta engine
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEInit( VEVOID )
{
  /* Engine already initialized */
  if (p_EngineInitialized)
    return TRUE;

  /* There are no manually configuration */
  if (!p_EngineManuallyConfigured)
  {
    VECONFIGURATION defaultConfiguration = VEGetConfigurationDefault();
    VEConfigure(&defaultConfiguration);
  }

  /* Store copy of engine settings */
  p_SettingsConfiguration = p_EngineConfiguration;

  /* Critical sections manager initialization */
  if (!VESectionInit(p_EngineConfiguration.m_MaximalCriticalSectionsNumber))
    return FALSE;

  /* Memory manager initialization */
  if (p_EngineConfiguration.m_EnableMemorySystem)
    if (!VEMemoryInit())
    {
      VEDeinit();
      return FALSE;
    }

  if (p_EngineConfiguration.m_EnableLogger)
    if (!VELoggerInit(p_EngineConfiguration.m_LoggingLevel, p_EngineConfiguration.m_LoggingToConsole))
    {
      VEDeinit();
      return FALSE;
    }

  /* Default managers */
  VELoggerInfo("Critical sections manager initialized");
  VELoggerInfo("Memory manager initialized");
  VELoggerInfo("Logger initialized");

  /* Strings manager initialization */
  if (!VEStringInit())
  {
    VEDeinit();
    return FALSE;
  }
  VELoggerInfo("Strings manager initialized");

  /* Arguments parser initialization */
  if (!VEArgumentInit())
  {
    VEDeinit();
    return FALSE;
  }
  VELoggerInfo("Arguments manager initialized");

  /* Threads manager initialization */
  if (!VEThreadInit(p_EngineConfiguration.m_MaximalThreadsNumber))
  {
    VEDeinit();
    return FALSE;
  }
  VELoggerInfo("Threads manager initialized");

  /* Pool manager initialization */
  if (!VEPoolInit(p_EngineConfiguration.m_MaximalPoolsNumber))
  {
    VEDeinit();
    return FALSE;
  }
  VELoggerInfo("Pools manager initialized");

  /* Database subsystem initialization */
  if (p_EngineConfiguration.m_EnableDatabaseSystem)
  {
    if (!VEDatabaseInit())
    {
      VEDeinit();
      return FALSE;
    }
    VELoggerInfo("Databases manager initialized");
  }

  /* Structures manager initialization */
  if (!VEStructureInit())
  {
    VEDeinit();
    return FALSE;
  }
  VELoggerInfo("Structures manager initialized");

  /* Trees manager initialization */
  if (!VETreeInit())
  {
    VEDeinit();
    return FALSE;
  }
  VELoggerInfo("Trees manager initialized");

  /* Graphics subsystem initialization */
  if (p_EngineConfiguration.m_EnableVideoSystem)
  {
    /* Console engine initialization */
    if (!VEConsoleInit(p_EngineConfiguration.m_ConsoleProcessor))
    {
      VEDeinit();
      return FALSE;
    }
    VELoggerInfo("Console initialized");

    /* Graphics engine initialization */
    if (!VEGraphicsInit(p_EngineConfiguration.m_VideoScreenWidth, p_EngineConfiguration.m_VideoScreenHeight, p_EngineConfiguration.m_EnableFullScreen))
    {
      VEDeinit();
      return FALSE;
    }
    VELoggerInfo("Graphics subsystem initialized");

    /* Storing configuration */
    p_SettingsGraphicsRenderAxes = p_EngineConfiguration.m_IsAxesVisible;

    /* Input manager */
    if (!VEInputInit(p_EngineConfiguration.m_KeyboardProcessor, p_EngineConfiguration.m_MouseProcessor))
    {
      VEDeinit();
      return FALSE;
    }
    VELoggerInfo("Input manager initialized");

    /* Texture manager */
    if (!VETextureInit())
    {
      VEDeinit();
      return FALSE;
    }
    VELoggerInfo("Textures manager initialized");

    /* Resource manager */
    if (!VEResourceInit())
    {
      VEDeinit();
      return FALSE;
    }
    VELoggerInfo("Resources manager initialized");

    /* Scenes manager */
    if (!VESceneInit())
    {
      VEDeinit();
      return FALSE;
    }
    VELoggerInfo("Scenes manager initialized");

    /* Interfaces manager */
    if (!VEInterfacesInit())
    {
      VEDeinit();
      return FALSE;
    }
    VELoggerInfo("Interfaces manager initialized");

    /* Menu manager initialization */
    if (!VEMenuInit())
    {
      VEDeinit();
      return FALSE;
    }
    VELoggerInfo("Menu manager initialized");
  }

  /* Setting configuring */
  p_SettingsGravityEnabled = p_EngineConfiguration.m_EnableMasses;

  /* Execute user-defined construcotr */
  if (p_EngineConfiguration.m_Constructor)
  {
    p_EngineConfiguration.m_Constructor();
    VELoggerInfo("Constructor called");
  }

  /* Setup engine started timestamp */
  p_SettingsEngineStarted = VESecondsSince1970();

  /* Engine configuration complete */
  VELoggerInfo("#");
  VELoggerInfo("");
  return TRUE;
} /* End of 'VEInit' function */

/***
 * PURPOSE: Deinitialize Venta engine
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEDeinit( VEVOID )
{
  /* Output separator */
  VELoggerInfo("");
  VELoggerInfo("#");

  /* Execute user-defined destructor */
  if (p_EngineConfiguration.m_Destructor)
  {
    p_EngineConfiguration.m_Destructor();
    VELoggerInfo("Destructor called");
  }

  /* Deinitialize menu manager */
  VEMenuDeinit();
  VELoggerInfo("Menu manager deinitialized");

  /* Deinitialize interfaces manager */
  VEInterfacesDeinit();
  VELoggerInfo("Interfaces manager deinitialized");

  /* Deinitialize scenes manager */
  VESceneDeinit();
  VELoggerInfo("Scenes manager deinitialized");

  /* Deinitialize resource manager */
  VEResourceDeinit();
  VELoggerInfo("Resources manager deinitialized");

  /* Deinitialize texture manager */
  VETextureDeinit();
  VELoggerInfo("Textures manager deinitialized");

  /* Deinitialize input manager */
  VEInputDeinit();
  VELoggerInfo("Input manager deinitialized");

  /* Deinitialize graphics manager */
  VEGraphicsDeinit();
  VELoggerInfo("Graphics manager deinitialized");

  /* Deinitialize console manager */
  VEConsoleDeinit();
  VELoggerInfo("Console manager deinitialized");

  /* Deinitialize trees manager */
  VETreeDeinit();
  VELoggerInfo("Trees manager deinitialized");

  /* Deinitialize structures manager */
  VEStructureDeinit();
  VELoggerInfo("Structures manager deinitialized");

  /* Deinitialize database manager */
  VEDatabaseDeinit();
  VELoggerInfo("Databases manager deinitialized");

  /* Deinitialize pools manager */
  VEPoolDeinit();
  VELoggerInfo("Pools manager deinitialized");

  /* Deinitialize threads manager */
  VEThreadDeinit();
  VELoggerInfo("Threads manager deinitialized");

  /* Deinitialize arguments manager */
  VEArgumentDeinit();
  VELoggerInfo("Arguments manager deinitialized");

  /* Deinitialize strings manager */
  VEStringDeinit();
  VELoggerInfo("Strings manager deinitialized");

  /* Output memory dump to current log file */
  MemoryDump();

  /* Deinitialize logger */
  VELoggerDeinit();

  /* Deinitialize memory manager */
  VEMemoryDeinit();

  /* Deinitialize critical sections */
  VESectionDeinit();

  /* Engine deinitiallized */
  p_EngineInitialized = FALSE;
  p_StopEngine = FALSE;
} /* End of 'VEDeinit' function */

/***
 * PURPOSE: Send stop signal for Venta Engine
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStop( VEVOID )
{
  p_StopEngine = TRUE;
} /* End of 'VEStop' function */

/***
 * PURPOSE: Check if stop signal was sent to engine
 *  RETURN: TRUE if stop signal was sent, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsStopping( VEVOID )
{
  return p_StopEngine;
} /* End of 'VEIsStopping' function */

/***
 * PURPOSE: Determine Venta engine state
 *  RETURN: TRUE if VE is running, FALSE if VE is stopping
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsRunning( VEVOID )
{
  return !p_StopEngine;
} /* End of 'VEIsRunning' function */
