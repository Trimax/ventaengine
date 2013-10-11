#include "internalarguments.h"
#include "internalcontainer.h"
#include "internalsettings.h"
#include "internalbuffer.h"
#include "memorymanager.h"
#include "arguments.h"

#include <string.h>

/* Database queries */
volatile static VEINTERNALCONTAINER *p_Arguments = NULL;

/* Command-line argument */
typedef struct tagVEARGUMENT
{
  VESTRING *m_Name;
  VESTRING *m_Value;
} VEARGUMENT;

/***
 * PURPOSE: Classify token as argument or not
 *  RETURN: TRUE if token is argument, FALSE otherwise
 *   PARAM: [IN] token - token to classify
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsArgument( const VEBUFFER token )
{
  return (VEBufferLength(token) > 1)&&(token[0] == '-');
} /* End of 'VEIsArgument' function */

/***
 * PURPOSE: Add argument with value to list
 *   PARAM: [IN] argument - argument's name
 *   PARAM: [IN] value    - argument's value
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEArgumentAdd( const VEBUFFER argument, const VEBUFFER value )
{
  VEARGUMENT *argValue = NULL;
  VEUINT i = 0, len = 0;

  /* Wrong argument */
  if (argument == NULL)
    return;

  argValue = New(sizeof(VEARGUMENT), "Command-line argument");
  if (!argValue)
    return;

  argValue->m_Name = VEStringCreateFromBufferInternal(argument);
  if (!argValue->m_Name)
  {
    Delete(argValue);
    return;
  }

  len = VEBufferLength(argument);
  for (i = 1; i < len; i++)
    argValue->m_Name->m_Data[i-1] = argValue->m_Name->m_Data[i];
  argValue->m_Name->m_Data[len-1] = 0;
  argValue->m_Name->m_Length--;

  if (value)
  {
    argValue->m_Value = VEStringCreateFromBufferInternal(value);
    if (!argValue->m_Value)
    {
      Delete(argValue->m_Name);
      Delete(argValue);
      return;
    }
  }

  /* Add created item to container */
  VEInternalContainerAdd((VEINTERNALCONTAINER*)p_Arguments, argValue);
} /* End of 'VEArgumentAdd' function */

/***
 * PURPOSE: Initialize arguments manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEArgumentInit( VEVOID )
{
  VEBUFFER currentArgument = NULL, currentValue = NULL;
  VEBOOL isValueExpected = FALSE;
  VEUINT argumentID = 0;

  /* There are no arguments defined */
  if (p_SettingsConfiguration.m_ArgsCount == 0)
    return TRUE;

  /* Allocate memory for arguments container */
  p_Arguments = VEInternalContainerCreate(p_SettingsConfiguration.m_ArgsCount, "Command-line arguments");
  if (!p_Arguments)
    return FALSE;

  /* Parse arguments */
  for (argumentID = 0; argumentID < p_SettingsConfiguration.m_ArgsCount; argumentID++)
  {
    VEBUFFER currentToken = p_SettingsConfiguration.m_Arguments[argumentID];
    if (VEBufferLength(currentToken) == 0)
      continue;

    /* What is it: argument or value? */
    if (VEIsArgument(currentToken))
    {
      VEArgumentAdd(currentArgument, currentValue);

      /* Switch pointers */
      currentArgument = currentToken;
      currentValue    = NULL;

      /* We expect value */
      isValueExpected = TRUE;
    } else
    {
      /* We expect argument again */
      if (!isValueExpected)
        continue;

      /* Store value */
      currentValue = currentToken;

      /* Next one should be argument */
      isValueExpected = FALSE;
    }
  }

  /* Add last argument with value */
  VEArgumentAdd(currentArgument, currentValue);

  /* That's it */
  return TRUE;
} /* End of 'VEArgumentsInit' function */

/***
 * PURPOSE: Delete argument
 *   PARAM: [IN] argument - allocated argument to delete
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEArgumentDelete( VEARGUMENT *arg )
{
  if (!arg)
    return;

  /* Release memory */
  VEStringDeleteInternal(arg->m_Name);
  VEStringDeleteInternal(arg->m_Value);
} /* End of 'VEArgumentDelete' function */

/***
 * PURPOSE: Deinitialize arguments manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEArgumentDeinit( VEVOID )
{
  /* There are no arguments */
  if (!p_Arguments)
    return;

  /* Remove all arguments */
  VEInternalContainerForeach((VEINTERNALCONTAINER*)p_Arguments, (VEFUNCTION)VEArgumentDelete);

  /* Remove internal container */
  VEInternalContainerDelete((VEINTERNALCONTAINER*)p_Arguments);

  /* That's it */
  p_Arguments = NULL;
} /* End of 'VEArgumentsDeinit' function */

/***
 * PURPOSE: Find argument in command line
 *  RETURN: TRUE if argument was defined in command-line, FALSE otherwise
 *   PARAM: [IN] name - argument name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEIsArgumentDefined( const VEBUFFER name )
{
  VEUINT pos = 0;
  VEBOOL isPresent = FALSE;

  VESectionEnterInternal(p_Arguments->m_Sync);

  for (pos = 0; (pos < p_Arguments->m_Size)&&(!isPresent); pos++)
    if (p_Arguments->m_Items[pos])
    {
      VEARGUMENT *arg = (VEARGUMENT*)p_Arguments->m_Items[pos];

      if (VEBuffersEqual(name, arg->m_Name->m_Data))
        isPresent = TRUE;
    }

  VESectionLeaveInternal(p_Arguments->m_Sync);

  /* That's it */
  return isPresent;
} /* End of 'VEIsArgumentDefined' function */

/***
 * PURPOSE: Find argument in command line
 *  RETURN: Argument value if argument defined, NULL otherwise
 *   PARAM: [IN] name - argument name
 * COMMENT: No need to release memory after using of function
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBUFFER VEArgumentGetValue( const VEBUFFER name )
{
  VEUINT pos = 0;
  VEARGUMENT *arg = NULL;

  VESectionEnterInternal(p_Arguments->m_Sync);

  for (pos = 0; (pos < p_Arguments->m_Size)&&(!arg); pos++)
    if (p_Arguments->m_Items[pos])
    {
      VEARGUMENT *currentArg = (VEARGUMENT*)p_Arguments->m_Items[pos];

      if (VEBuffersEqual(name, currentArg->m_Name->m_Data))
        arg = currentArg;
    }

  VESectionLeaveInternal(p_Arguments->m_Sync);

  /* Argument wasn't found */
  if (!arg)
    return NULL;

  /* It's a option (flag) */
  if (!arg->m_Value)
    return NULL;

  /* That's it */
  return arg->m_Value->m_Data;
} /* End of 'VEIsArgumentDefined' function */
