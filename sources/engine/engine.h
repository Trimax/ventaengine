#ifndef ENGINE_H_INCLUDED
#define ENGINE_H_INCLUDED

#include "structuresmanager.h"
#include "interfacemanager.h"
#include "resourcemanager.h"
#include "criticalsection.h"
#include "texturemanager.h"
#include "configuration.h"
#include "memorymanager.h"
#include "shadermanager.h"
#include "scenemanager.h"
#include "environment.h"
#include "skinmanager.h"
#include "treemanager.h"
#include "arguments.h"
#include "billboard.h"
#include "database.h"
#include "physics.h"
#include "terrain.h"
#include "console.h"
#include "string.h"
#include "object.h"
#include "logger.h"
#include "thread.h"
#include "camera.h"
#include "light.h"
#include "types.h"
#include "time.h"
#include "pool.h"
#include "math.h"
#include "menu.h"
#include "fog.h"

/***
 * PURPOSE: Get default configuration
 *  RETURN: Default configuration
 *  AUTHOR: Eliseev Dmitry
 ***/
VECONFIGURATION VEGetConfigurationDefault( VEVOID );

/***
 * PURPOSE: Set Venta engine configuration
 *   PARAM: [IN] configuration - pointer to filled configuration
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEConfigure( const VECONFIGURATION *configuration );

/***
 * PURPOSE: Initialize Venta engine
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEInit( VEVOID );

/***
 * PURPOSE: Start main loop
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VERun( VEVOID );

/***
 * PURPOSE: Send stop signal for Venta Engine
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEStop( VEVOID );

/***
 * PURPOSE: Determine Venta engine state
 *  RETURN: TRUE if VE is running, FALSE if VE is stopping
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsRunning( VEVOID );

/***
 * PURPOSE: Deinitialize Venta engine
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEDeinit( VEVOID );

#endif // ENGINE_H_INCLUDED
