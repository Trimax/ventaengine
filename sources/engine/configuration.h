#ifndef CONFIGURATION_H_INCLUDED
#define CONFIGURATION_H_INCLUDED

#include "types.h"
#include "input.h"

/* Configuration structure definition */
typedef struct tagVECONFIGURATION
{
  /*** Subsystems ***/
  VEBOOL m_EnableLogger;         /* Enable logging */
  VEBOOL m_EnableVideoSystem;    /* Enable video subsystem functions */
  VEBOOL m_EnableSoundSystem;    /* Enable audio subsystem functions */
  VEBOOL m_EnableMemorySystem;   /* Enable memory observing functions */
  VEBOOL m_EnableNetworkSystem;  /* Enable network subsystem functions */
  VEBOOL m_EnableDatabaseSystem; /* Enable database subsystem functions */

  /*** Multitasks configuration ***/
  VEUSHORT m_MaximalThreadsNumber;          /* Maximal allowed number of threads */
  VEUSHORT m_MaximalCriticalSectionsNumber; /* Maximal allowed number of critical sections */
  VEUSHORT m_MaximalPoolsNumber;            /* Maximal allowed number of pools */

  /*** Video subsystem configuration ***/
  VEUSHORT m_VideoScreenWidth;   /* Screen width */
  VEUSHORT m_VideoScreenHeight;  /* Screen height */
  VEBOOL   m_EnableFullScreen;   /* Full screen mode */
  VEBOOL   m_EnableShaders;      /* Enable shaders */
  VEBOOL   m_IsAxesVisible;      /* Draw axes */

  /*** Physics engine configuration ***/
  VEBOOL   m_EnableMasses;       /* Enable mass interconnection */
  VEBOOL   m_EnableCharges;      /* Enable electric interconnection */
  VEBOOL   m_EnableTemperatures; /* Enable temperature interconnection */

  /*** Logger configuration ***/
  VEBYTE m_LoggingLevel;     /* Logging level (SEE VE_LOGLEVEL_XXX definitions) */
  VEBOOL m_LoggingToConsole; /* Enable logging to console */

  /*** Call-back functions ***/
  VEPROCEDURE                m_Constructor;       /* Call-bakc for application initialization */
  VEPROCEDURE                m_Destructor;        /* Call-back for application deinitialization */
  VEKEYBOARDPROCESSOR        m_KeyboardProcessor; /* Call-back for keyboard processing */
  VEMOUSEPROCESSOR           m_MouseProcessor;    /* Call-back for mouse processing */
  VEFUNCTIONCONSOLEPROCESSOR m_ConsoleProcessor;  /* Call-back for console processing */

  /*** Command-line arguments ***/
  VEINT     m_ArgsCount; /* Number of command-line arguments */
  VEBUFFER *m_Arguments; /* Command-line arguments */
} VECONFIGURATION;

#endif // CONFIGURATION_H_INCLUDED
