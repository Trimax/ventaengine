#ifdef _WIN32
#include <winsock.h>
#endif

#include "mysql/mysql.h"

#include "internalcriticalsection.h"
#include "internalcontainer.h"
#include "internalstring.h"
#include "internalbuffer.h"
#include "memorymanager.h"
#include "database.h"
#include "string.h"

#include <string.h>

/* Database connections */
volatile static VEINTERNALCONTAINER *p_DatabaseContainer = NULL;

/* Database queries */
volatile static VEINTERNALCONTAINER *p_DatabaseQueries = NULL;

/* MySQL connection structure */
typedef struct tagVEDATABASE
{
  MYSQL             *m_Socket;  /* MySQL connection socket */
  VECRITICALSECTION *m_Sync;    /* Current connection critical section */
} VEDATABASE;

/* MySQL query structure */
typedef struct tagVEQUERY
{
  VECRITICALSECTION  *m_Sync;    /* Current query critical section */
  VEBOOL              m_Success; /* Was query successfull? */
  VEUINT              m_NumCols; /* Number of fetched colums */
  VEUINT              m_NumRows; /* Number of fetched rows */
  VESTRING         ***m_Data;    /* Fetched data */
  VESTRING          **m_Fields;  /* Fields names */
} VEQUERY;

/***
 * PURPOSE: Initialize database connection manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEDatabaseInit( VEVOID )
{
  /* Already initialized */
  if (p_DatabaseContainer)
    return TRUE;

  p_DatabaseContainer = VEInternalContainerCreate(VE_DEFAULT_CONTAINERSIZE, "Database connections container");
  if (!p_DatabaseContainer)
    return FALSE;
  return TRUE;
} /* End of 'VEDatabaseInit' function */

/***
 * PURPOSE: Establish connection with DB server and select DB for internal use
 *  RETURN: Established connection identifier if success, 0 otherwise
 *   PARAM: [IN] host     - DB host
 *   PARAM: [IN] port     - DB service port
 *   PARAM: [IN] username - DB server username for login
 *   PARAM: [IN] password - DB server password for login
 *   PARAM: [IN] database - DB to use
 *  AUTHOR: Eliseev Dmitry
 ***/
VEDATABASE *VEDatabaseConnectInternal( const VEBUFFER host, const VEUINT port, const VEBUFFER username, const VEBUFFER password, const VEBUFFER database )
{
  VEDATABASE *newDBConnection = NULL;
  MY_CHARSET_INFO charset;
  memset(&charset, 0, sizeof(MY_CHARSET_INFO));

  if (!(host&&username&&password&&database))
    return NULL;
  if (port == 0)
    return NULL;

  newDBConnection = New(sizeof(VEDATABASE), "DB Connection");
  if (!newDBConnection)
    return NULL;

  /* Create critical section */
  newDBConnection->m_Sync = VESectionCreateInternal("DB connection internal critical section");
  if (!newDBConnection->m_Sync)
  {
    Delete(newDBConnection);
    return NULL;
  }

  /* Establish real connection to server */
  newDBConnection->m_Socket = mysql_init((MYSQL*)0);
  newDBConnection->m_Socket = mysql_real_connect(newDBConnection->m_Socket, host, username, password, database, port, NULL, CLIENT_MULTI_STATEMENTS);
  if (!newDBConnection->m_Socket)
  {
    VESectionDeleteInternal(newDBConnection->m_Sync);
    Delete(newDBConnection);
    return NULL;
  }

  /* Set connection collation */
  mysql_set_character_set(newDBConnection->m_Socket, "utf8");

  /* Set sending to client data character set */
  mysql_query(newDBConnection->m_Socket, "SET character_set_results='cp866';");

  /* Set sending from client data character set */
  mysql_query(newDBConnection->m_Socket, "SET character_set_client='cp866';");

  /* Set character set to transform data before query execution */
  mysql_query(newDBConnection->m_Socket, "SET character_set_connection='utf8';");

  /* Selecting Database */
  if (mysql_select_db(newDBConnection->m_Socket, database) < 0)
  {
    mysql_close(newDBConnection->m_Socket);
    VESectionDeleteInternal(newDBConnection->m_Sync);
    Delete(newDBConnection);
    return NULL;
  }

  /* Request connection charset */
  mysql_get_character_set_info(newDBConnection->m_Socket, &charset);

  /* That's it */
  return newDBConnection;
} /* End of 'VEDatabaseConnectInternal' function */

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
VEUINT VEDatabaseConnect( const VEBUFFER host, const VEUINT port, const VEBUFFER username, const VEBUFFER password, const VEBUFFER database )
{
  VEDATABASE *newDBConnection = NULL;

  if (!p_DatabaseContainer)
    return 0;

  newDBConnection = VEDatabaseConnectInternal(host, port,username, password, database);
  if (!newDBConnection)
    return 0;

  return VEInternalContainerAdd((VEINTERNALCONTAINER*)p_DatabaseContainer, (VEPOINTER)newDBConnection);
} /* End of 'VEDatabaseConnect' function */

VEVOID VEDatabaseDisconnectInternal( VEDATABASE *curDBConnection )
{
  if (!curDBConnection)
    return;

  /* Disconnect from DB server */
  VESectionEnterInternal(curDBConnection->m_Sync);
  mysql_close(curDBConnection->m_Socket);
  VESectionLeaveInternal(curDBConnection->m_Sync);

  /* Remove critical section */
  VESectionDeleteInternal(curDBConnection->m_Sync);

  /* Self deletion */
  Delete(curDBConnection);
} /* End of 'VEDatabaseDisconnectInternal' function */

/***
 * PURPOSE: Close connection to DB server
 *   PARAM: [IN] connectionID - established connection identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEDatabaseDisconnect( const VEUINT connectionID )
{
  VEDATABASE *curDBConnection = NULL;

  if (!p_DatabaseContainer)
    return;

  /* Obtain connection */
  curDBConnection = VEInternalContainerGet((VEINTERNALCONTAINER*)p_DatabaseContainer, connectionID);
  if (!curDBConnection)
    return;

  /* Remove connection from container */
  VEInternalContainerRemove((VEINTERNALCONTAINER*)p_DatabaseContainer, connectionID);

  VEDatabaseDisconnectInternal(curDBConnection);
} /* End of 'VEDatabaseDisconnect' function */

/***
 * PURPOSE: Initialize database connection manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEDatabaseDeinit( VEVOID )
{
  if (p_DatabaseContainer)
  {
    VEInternalContainerForeach((VEINTERNALCONTAINER*)p_DatabaseContainer, (VEFUNCTION)VEDatabaseDisconnectInternal);
    VEInternalContainerDelete((VEINTERNALCONTAINER*)p_DatabaseContainer);
  }

  p_DatabaseContainer = NULL;
} /* End of 'VEDatabaseDeinit' function */

/***
 * PURPOSE: Delete query block in memory
 *   PARAM: [IN] query - pointer to existing memory block with query
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEQueryDeleteInternal( VEQUERY *query )
{
  if (!query)
    return;

  VESectionEnterInternal(query->m_Sync);

  if (query->m_Data)
  {
    VEUINT row = 0, col = 0;
    for (row = 0; row < query->m_NumRows; row++)
    {
      for (col = 0; col < query->m_NumCols; col++)
        VEStringDeleteInternal(query->m_Data[row][col]);
      Delete(query->m_Data[row]);
    }

    Delete(query->m_Data);
  }

  if (query->m_Fields)
  {
    VEUINT col = 0;
    for (col = 0; col < query->m_NumCols; col++)
      VEStringDeleteInternal(query->m_Fields[col]);
    Delete(query->m_Fields);
  }

  VESectionLeaveInternal(query->m_Sync);
  VESectionDeleteInternal(query->m_Sync);
  Delete(query);
} /* End of '' function */

/***
 * PURPOSE: Create query block in memory
 *  RETURN: Created query block
 *   PARAM: [IN] numRows - number of rows
 *   PARAM: [IN] numCols - number of columns
 *  AUTHOR: Eliseev Dmitry
 ***/
VEQUERY *VEQueryCreateInternal( const VEUINT numRows, const VEUINT numCols )
{
  VEQUERY *query = New(sizeof(VEQUERY), "Query result");
  VEUINT row = 0;

  query->m_NumCols = numCols;
  query->m_NumRows = numRows;
  query->m_Success = TRUE;

  query->m_Sync = VESectionCreateInternal("DB query internal critical section");
  if (!query->m_Sync)
  {
    Delete(query);
    return NULL;
  }

  /* Done */
  if (query->m_NumCols * query->m_NumRows == 0)
    return query;

  /* Rows allocation */
  query->m_Data = New(sizeof(VESTRING**) * query->m_NumRows, "Query result rows");
  if (!query->m_Data)
  {
    VEQueryDeleteInternal(query);
    return NULL;
  }

  /* Rows allocation */
  for (row = 0; row < query->m_NumRows; row++)
  {
    query->m_Data[row] = New(sizeof(VESTRING*) * query->m_NumCols, "Query result row");
    if (!query->m_Data[row])
    {
      VEQueryDeleteInternal(query);
      return NULL;
    }
  }

  /* That's it */
  return query;
} /* End of 'VEQueryCreateInternal' function */

/***
 * PURPOSE: Create and execute SQL query
 *  RETURN: Created query identifier if success, 0 otherwise
 *   PARAM: [IN] connectionID - established connection identifier
 *   PARAM: [IN] query        - SQL query statement
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEDatabaseQueryCreate( const VEUINT connectionID, const VEBUFFER query )
{
  VEUINT queryResult = 0, row = 0, col = 0;
  VEQUERY *result = NULL;
  MYSQL_RES *resource = NULL;

  /* Check arguments */
  VEDATABASE *connection = VEInternalContainerGet((VEINTERNALCONTAINER*)p_DatabaseContainer, connectionID);
  if (!(connection && query))
    return 0;

  VESectionEnterInternal(connection->m_Sync);

  /* Execute query */
  queryResult = mysql_real_query(connection->m_Socket, query, VEBufferLength(query));
  if (queryResult != 0)
  {
    VESectionLeaveInternal(connection->m_Sync);
    return 0;
  }

  /* New query result memory allocation */
  result = VEQueryCreateInternal(0, 0);
  if (!result)
  {
    VESectionLeaveInternal(connection->m_Sync);
    return 0;
  }

  /* Determine the number of rows */
  result->m_NumRows = mysql_affected_rows(connection->m_Socket);
  result->m_NumCols = mysql_field_count(connection->m_Socket);

  /* Probably, it was empty SELECT or UPDATE or INSERT or DELETE etc */
  if (result->m_NumCols == 0)
  {
    VESectionLeaveInternal((VECRITICALSECTION*)connection->m_Sync);
    return VEInternalContainerAdd((VEINTERNALCONTAINER*)p_DatabaseQueries, result);
  }

  /* Trying to store result */
  resource = mysql_store_result(connection->m_Socket);
  if (!resource)
  {
    VEQueryDeleteInternal(result);
    VESectionLeaveInternal(connection->m_Sync);
    return 0;
  }

  /* Empty SELECT it's normal */
  if (result->m_NumRows == 0)
  {
    mysql_free_result(resource);
    VESectionLeaveInternal(connection->m_Sync);
    return VEInternalContainerAdd((VEINTERNALCONTAINER*)p_DatabaseQueries, result);
  }

  /* Memory allocation for rows */
  result->m_Data = New(sizeof(VESTRING**) * result->m_NumRows, "DB Data rows");
  if (!result->m_Data)
  {
    mysql_free_result(resource);
    VEQueryDeleteInternal(result);
    VESectionLeaveInternal(connection->m_Sync);
    return 0;
  }

  /* Copying data */
  for (row = 0; row < result->m_NumRows; row++)
  {
    MYSQL_ROW currentRow = mysql_fetch_row(resource);
    VEULONG *lengths = mysql_fetch_lengths(resource);

    /* Allocate memory per row */
    result->m_Data[row] = New(sizeof(VESTRING*) * result->m_NumCols, "DB Data row");
    if (!result->m_Data[row])
    {
      VEQueryDeleteInternal(result);
      mysql_free_result(resource);
      Delete(result);
      VESectionLeaveInternal(connection->m_Sync);
      return 0;
    }

    /* Copying data */
    for (col = 0; col < result->m_NumCols; col++)
    {
      result->m_Data[row][col] = VEStringCreateInternal(lengths[col] + 1);
      memcpy(result->m_Data[row][col]->m_Data, currentRow[col], lengths[col]);
      result->m_Data[row][col]->m_Length = lengths[col];
    }
  }

  /* Extracting column names */
  if (result->m_NumCols > 0)
  {
    MYSQL_FIELD *fields = mysql_fetch_fields(resource);
    result->m_Fields = New(sizeof(VESTRING*) * result->m_NumCols, "Query result fields array");
    if (!result->m_Fields)
    {
      mysql_free_result(resource);
      VEQueryDeleteInternal(result);
      VESectionLeaveInternal(connection->m_Sync);
      return 0;
    }

    /* Memory allocation and copying data */
    for (col = 0; col < result->m_NumCols; col++)
    {
      result->m_Fields[col] = VEStringCreateInternal(fields[col].name_length + 1);
      if (!result->m_Fields[col])
      {
        mysql_free_result(resource);
        VEQueryDeleteInternal(result);
        VESectionLeaveInternal(connection->m_Sync);
        return 0;
      }

      /* Copying data */
      memcpy(result->m_Fields[col]->m_Data, fields[col].name, fields[col].name_length);
      result->m_Fields[col]->m_Length = fields[col].name_length;
    }
  }

  /* Release results and leave critical section */
  mysql_free_result(resource);
  VESectionLeaveInternal(connection->m_Sync);


  return 0;
} /* End of 'VEDatabaseQueryCreate' function */

/***
 * PURPOSE: Delete query result
 *   PARAM: [IN] queryID - created query
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEDatabaseQueryDelete( const VEUINT queryID )
{
  VEQUERY *query = VEInternalContainerGet((VEINTERNALCONTAINER*)p_DatabaseQueries, queryID);
  if (!query)
    return;

  /* Remove item from container */
  VEInternalContainerRemove((VEINTERNALCONTAINER*)p_DatabaseQueries, queryID);

  /* Release memory */
  VEQueryDeleteInternal(query);
} /* End of 'VEDatabaseQueryDelete' function */

/***
 * PURPOSE: Determine was query successfully
 *  RETURN: TRUE if query was successful, FALSE otherwise
 *   PARAM: [IN] queryID - created query identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEDatabaseQueryIsSuccessful( const VEUINT queryID )
{
  VEQUERY *query = VEInternalContainerGet((VEINTERNALCONTAINER*)p_DatabaseQueries, queryID);
  if (!query)
    return FALSE;
  return query->m_Success;
} /* End of 'VEDatabaseQueryIsSuccessful' function */

/***
 * PURPOSE: Determine number of query rows
 *  RETURN: Number of accepted rows
 *   PARAM: [IN] queryID - created query identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEDatabaseQueryGetRowsNumber( const VEUINT queryID )
{
  VEQUERY *query = VEInternalContainerGet((VEINTERNALCONTAINER*)p_DatabaseQueries, queryID);
  if (!query)
    return 0;
  return query->m_NumRows;
} /* End of 'VEDatabaseQueryGetRowsNumber' function */

/***
 * PURPOSE: Determine number of query cols
 *  RETURN: Number of accepted cols if success, 0 otherwise
 *   PARAM: [IN] queryID - created query identifier
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEDatabaseQueryGetColsNumber( const VEUINT queryID )
{
  VEQUERY *query = VEInternalContainerGet((VEINTERNALCONTAINER*)p_DatabaseQueries, queryID);
  if (!query)
    return 0;
  return query->m_NumCols;
} /* End of 'VEDatabaseQueryGetColsNumber' function */

/***
 * PURPOSE: Get column ID by it's name
 *  RETURN: Column ID if success, number of columns otherwise
 *   PARAM: [IN] query      - pointer to query result
 *   PARAM: [IN] columnName - column name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEDatabaseQueryGetColByNameInternal( const VEQUERY *query, const VEBUFFER columnName )
{
  VEUINT colID = 0;
  for (colID = 0; colID < query->m_NumCols; colID++)
    if (!strcasecmp(query->m_Fields[colID]->m_Data, columnName))
      return colID;
  return colID;
} /* End of 'VEDatabaseQueryGetColByNameInternal' function */

/***
 * PURPOSE: Get query row by it's number
 *  RETURN: Query result row if success, NULL otherwise
 *   PARAM: [IN] queryID - created query identifier
 *   PARAM: [IN] rowID   - row number
 *  AUTHOR: Eliseev Dmitry
 ***/
VERESULT *VEDatabaseQueryGetRow( const VEUINT queryID, const VEUINT rowID )
{
  VEUINT colID = 0;
  VERESULT *result = NULL;
  VEQUERY *query = VEInternalContainerGet((VEINTERNALCONTAINER*)p_DatabaseQueries, queryID);
  if (!query)
    return NULL;

  /* Wrong identifier */
  if (rowID >= query->m_NumRows)
    return NULL;

  result = New(sizeof(VERESULT), "Query row");
  if (!result)
    return NULL;

  result->m_NumItems = query->m_NumCols;
  result->m_Items = New(sizeof(VEDATA) * query->m_NumCols, "Query row items");
  if (!result->m_Items)
  {
    Delete(result);
    return NULL;
  }

  /* Copying data */
  for (colID = 0; colID < query->m_NumCols; colID++)
  {
    result->m_Items[colID].m_Data = query->m_Data[rowID][colID]->m_Data;
    result->m_Items[colID].m_Size = query->m_Data[rowID][colID]->m_Length;
  }

  /* That's it */
  return result;
} /* End of 'VEDatabaseQueryGetRow' function */

/***
 * PURPOSE: Get query col by it's name
 *  RETURN: Query result col if success, NULL otherwise
 *   PARAM: [IN] queryID - created query identifier
 *   PARAM: [IN] colName - column name
 *  AUTHOR: Eliseev Dmitry
 ***/
VERESULT *VEDatabaseQueryGetCol( const VEUINT queryID, const VEBUFFER colName )
{
  VERESULT *result = NULL;
  VEUINT i = 0;
  VEINT  columnID = -1;
  VEQUERY *query = VEInternalContainerGet((VEINTERNALCONTAINER*)p_DatabaseQueries, queryID);
  if (!query)
    return NULL;
  if (!colName)
    return NULL;

  /* Determine column number */
  columnID = -1;
  for (i = 0; (i < query->m_NumCols)&&(columnID == -1); i++)
    if (!strcasecmp(colName, query->m_Fields[i]->m_Data))
      columnID = i;

  if (columnID == -1)
    return NULL;


  result = New(sizeof(VERESULT), "Query column");
  if (!result)
    return NULL;

  result->m_NumItems = query->m_NumRows;
  result->m_Items = New(sizeof(VEDATA) * query->m_NumRows, "Query column items");
  if (!result->m_Items)
  {
    Delete(result);
    return NULL;
  }

  /* Copying data */
  for (i = 0; i < query->m_NumRows; i++)
  {
    result->m_Items[i].m_Data = query->m_Data[i][columnID]->m_Data;
    result->m_Items[i].m_Size = query->m_Data[i][columnID]->m_Length;
  }

  /* That's it */
  return result;
} /* End of 'VEDatabaseQueryGetCol' function */

/***
 * PURPOSE: Get query cell by it's name and row number
 * COMMENT: No need to release memory after using result
 *  RETURN: Query result cell if success, NULL otherwise
 *   PARAM: [IN] queryID - created query identifier
 *   PARAM: [IN] rowID   - row number
 *   PARAM: [IN] colName - column name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEDATA VEDatabaseQueryGetCell( const VEUINT queryID, const VEUINT rowID, const VEBUFFER colName )
{
  VEDATA result;
  VEUINT colID = 0;
  VEQUERY *query = VEInternalContainerGet((VEINTERNALCONTAINER*)p_DatabaseQueries, queryID);
  memset(&result, 0, sizeof(VEDATA));
  if (!query)
    return result;

  /* Wrong row number */
  if (rowID >= query->m_NumRows)
    return result;

  colID = VEDatabaseQueryGetColByNameInternal(query, colName);
  if (colID == query->m_NumCols)
    return result;

  result.m_Data = query->m_Data[rowID][colID]->m_Data;
  result.m_Size = query->m_Data[rowID][colID]->m_Length;

  /* That's it */
  return result;
} /* End of 'VEDatabaseQueryGetCell' function */

/***
 * PURPOSE: Release query result
 *   PARAM: [IN] result - pointer to result, obtained by GetRow / GetCol functions
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEDatabaseQueryResultRelease( VERESULT *result )
{
  if (!result)
    return;

  if (result->m_Items)
    Delete(result->m_Items);
  Delete(result);
} /* End of 'VEDatabaseQueryResultRelease' function */
