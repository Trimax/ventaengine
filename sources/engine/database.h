#ifndef DATABASE_H_INCLUDED
#define DATABASE_H_INCLUDED

#include "types.h"

typedef struct tagVEDATA
{
  VEUINT   m_Size; /* Buffer size */
  VEBUFFER m_Data; /* Buffer data */

} VEDATA;

/* Structure to store result of GetRow, GetCol */
typedef struct tagVERESULT
{
  VEUINT     m_NumItems;
  VEDATA    *m_Items;
} VERESULT;

/***
 * PURPOSE: Establish connection with DB server and select DB to use
 *  RETURN: Established connection identifier if success, 0 otherwise
 *   PARAM: [IN] host     - DB host
 *   PARAM: [IN] port     - DB service port
 *   PARAM: [IN] username - DB server username for login
 *   PARAM: [IN] password - DB server password for login
 *   PARAM: [IN] database - DB to use
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEDatabaseConnect( const VEBUFFER host, const VEUINT port, const VEBUFFER username, const VEBUFFER password, const VEBUFFER database );

/***
 * PURPOSE: Create and execute SQL query
 *  RETURN: Created query identifier if success, 0 otherwise
 *   PARAM: [IN] connectionID - established connection identifier
 *   PARAM: [IN] query        - SQL query statement
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEDatabaseQueryCreate( const VEUINT connectionID, const VEBUFFER query );

/***
 * PURPOSE: Determine was query successfully
 *  RETURN: TRUE if query was successful, FALSE otherwise
 *   PARAM: [IN] queryID - created query identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEDatabaseQueryIsSuccessful( const VEUINT queryID );

/***
 * PURPOSE: Determine number of query rows
 *  RETURN: Number of accepted rows if success, 0 otherwise
 *   PARAM: [IN] queryID - created query identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEDatabaseQueryGetRowsNumber( const VEUINT queryID );

/***
 * PURPOSE: Determine number of query cols
 *  RETURN: Number of accepted cols if success, 0 otherwise
 *   PARAM: [IN] queryID - created query identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEDatabaseQueryGetColsNumber( const VEUINT queryID );

/***
 * PURPOSE: Get query row by it's number
 *  RETURN: Query result row if success, NULL otherwise
 *   PARAM: [IN] queryID - created query identifier
 *   PARAM: [IN] rowID   - row number
 *  AUTHOR: Eliseev Dmitry
 ***/
VERESULT *VEDatabaseQueryGetRow( const VEUINT queryID, const VEUINT rowID );

/***
 * PURPOSE: Get query col by it's name
 *  RETURN: Query result col if success, NULL otherwise
 *   PARAM: [IN] queryID - created query identifier
 *   PARAM: [IN] colName - column name
 *  AUTHOR: Eliseev Dmitry
 ***/
VERESULT *VEDatabaseQueryGetCol( const VEUINT queryID, const VEBUFFER colName );

/***
 * PURPOSE: Release query result
 *   PARAM: [IN] result - pointer to result, obtained by GetRow / GetCol functions
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEDatabaseQueryResultRelease( VERESULT *result );

/***
 * PURPOSE: Get query cell by it's name and row number
 * COMMENT: No need to release memory after using result
 *  RETURN: Query result cell if success, NULL otherwise
 *   PARAM: [IN] queryID - created query identifier
 *   PARAM: [IN] rowID   - row number
 *   PARAM: [IN] colName - column name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEDATA VEDatabaseQueryGetCell( const VEUINT queryID, const VEUINT rowID, const VEBUFFER colName );

/***
 * PURPOSE: Delete query result
 *   PARAM: [IN] queryID - created query
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEDatabaseQueryDelete( const VEUINT queryID );

/***
 * PURPOSE: Close connection to DB server
 *   PARAM: [IN] connectionID - established connection identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEDatabaseDisconnect( const VEUINT connectionID );

#endif // DATABASE_H_INCLUDED
