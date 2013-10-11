#ifndef CRITICALSECTION_H_INCLUDED
#define CRITICALSECTION_H_INCLUDED

#include "types.h"

/***
 * PURPOSE: Wait while critical section is busy and after enter in it
 *   PARAM: [IN] sectionID - critical section identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionEnter( const VEUINT sectionID );

/***
 * PURPOSE: Leave critical section
 *   PARAM: [IN] sectionID - critical section identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VESectionLeave( const VEUINT sectionID );

#endif // CRITICALSECTION_H_INCLUDED
