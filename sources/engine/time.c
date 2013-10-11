#ifdef _WIN32
#include <windows.h>
#include <time.h>
#else
#define _XOPEN_SOURCE 500
#include <unistd.h>
#include <sys/timeb.h>
#include <unistd.h>
#endif

#include <stdio.h>

#include "internalsettings.h"
#include "time.h"

#define VE_MILLITOMICRO(x) ((x)*1000)

/***
 * PURPOSE: Pause current thread for some time
 *   PARAM: [IN] milliseconds - pause size
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEWait( const VEULONG milliseconds )
{
  #ifdef _WIN32
  Sleep(milliseconds);
  #else
  usleep(VE_MILLITOMICRO(milliseconds));
  #endif
} /* End of 'Wait' function */

/***
 * PURPOSE: Determine number of seconds since 1970
 *  RETURN: Number of seconds since 1970
 *  AUTHOR: Eliseev Dmitry
 ***/
VEULONG VESecondsSince1970( VEVOID )
{
  #ifdef _WIN32
  time_t now;
  time(&now);

  return (VEREAL)now * 1000.0;
  #else
  struct timeb now;
  ftime(&now);

  return now.time;
  #endif
} /* End of 'VESecondsSince1970' function */

/***
 * PURPOSE: Determine number of milliseconds since application started
 *  RETURN: Number of milliseconds since application started
 *  AUTHOR: Eliseev Dmitry
 ***/
VEULONG VETime( VEVOID )
{
  #ifdef _WIN32
  time_t now;
  time(&now);

  return now * 1000.0 - p_SettingsEngineStarted;
  #else
  struct timeb now;
  ftime(&now);

  return (now.time - p_SettingsEngineStarted) * 1000 + now.millitm;
  #endif
} /* End of 'VETime' function */
