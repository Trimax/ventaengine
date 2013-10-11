#ifndef INTERNALDATABASE_H_INCLUDED
#define INTERNALDATABASE_H_INCLUDED

/***
 * PURPOSE: Initialize database connection manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEDatabaseInit( VEVOID );

/***
 * PURPOSE: Initialize database connection manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEDatabaseDeinit( VEVOID );

#endif // INTERNALDATABASE_H_INCLUDED
