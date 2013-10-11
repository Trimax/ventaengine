#ifdef _WIN32
#include <windows.h>
#else
#include <dirent.h>
#endif

#include "internalresourcemanager.h"
#include "internalcontainer.h"
#include "resourcemanager.h"
#include "internalheader.h"
#include "internalbuffer.h"
#include "internalstring.h"
#include "internallist.h"
#include "memorymanager.h"
#include "internalfs.h"
#include "console.h"

#include <string.h>
#include <stdlib.h>
#include <stdio.h>

/* Resources */
volatile static VEINTERNALCONTAINER *p_ResourceContainer = NULL;

/*** File chunk structure ***/
typedef struct tagVECHUNK
{
  VEULONG   m_Size;     /* Chunk data size */
  VESTRING *m_Name;     /* Chunk file name */
} VECHUNK;

/***
 * PURPOSE: Build new path and normalize it using two subparts
 *  RETURN: Pointer to new path if success, NULL otherwise
 *   PARAM: [IN] left  - left subpart
 *   PARAM: [IN] right - right subpart
 *  AUTHOR: Eliseev Dmitry
 ***/
VESTRING *VEPathBuild( const VEBUFFER left, const VEBUFFER right )
{
  VEUINT lenLeft  = VEBufferLength(left);
  VEUINT lenRight = VEBufferLength(right);
  VESTRING *fullPath = NULL;

  /* Wrong arguments */
  if (!(left&&right))
    return NULL;

  /* Create output path variable */
  fullPath = VEStringCreateInternal(lenLeft + lenRight + 2);
  if (!fullPath)
    return NULL;

  /* Replace backslashes with slashes */
  VEBufferReplaceAll(left, '\\', '/');
  VEBufferReplaceAll(right, '\\', '/');

  /* Copying left part */
  memcpy(fullPath->m_Data, left, lenLeft);

  /* Remove slashes from the begining and ending of string */
  VEBufferTrim(right, '/');

  /* Copying right part */
  if (left[lenLeft-1] == '/')
    memcpy(&fullPath->m_Data[lenLeft], right, lenRight);
  else
  {
    fullPath->m_Data[lenLeft] = '/';
    memcpy(&fullPath->m_Data[lenLeft+1], right, lenRight);
  }

  /* That's it */
  fullPath->m_Length = VEBufferLength(fullPath->m_Data);
  return fullPath;
} /* End of 'VEPathBuild' function */

/***
 * PURPOSE: List all files inside the directory
 *  RETURN: List of files if success, NULL otherwise
 *   PARAM: [IN] directory - directory to list
 *  AUTHOR: Eliseev Dmitry
 ***/
VELIST *VEDirectoryList( const VEBUFFER directory )
{
  VELIST *filesList = VEListCreateInternal(0, NULL);

#ifdef _WIN32
  VECHAR mask[] = "/*.*";
  HANDLE hFind;
  WIN32_FIND_DATA data;
  VEBOOL bContinue = TRUE;
  VESTRING *searchPath = VEPathBuild(directory, mask);
  if (!searchPath)
  {
    VEListDeleteInternal(filesList);
    return NULL;
  }

  /* Search handle */
  hFind = FindFirstFile(searchPath->m_Data, &data);

  /* Loop through all files */
  while (hFind && bContinue) {
	  if (!(data.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)) {
      VESTRING *fileName = VEStringCreateFromBufferInternal(data.cFileName);
      VELIST *node = VEListCreateInternal(0, fileName);
      VEListPutInternal(filesList, node);
    }

    /* Going to the next object */
	  bContinue = FindNextFile(hFind, &data);
  }

  /* Release memory */
  FindClose(hFind);
  VEStringDeleteInternal(searchPath);
#else
  DIR *dpdf;
  struct dirent *epdf;

  /* Wrong arguments */
  if (!directory)
  {
    VEListDeleteInternal(filesList);
    return NULL;
  }

  /* Try to open directory */
  dpdf = opendir(directory);
  if (!dpdf)
    return NULL;

  /* Reading entire directory */
  while ((epdf = readdir(dpdf)) != FALSE)
  {
    if (epdf->d_name[0] != '.')
    {
      VESTRING *fileName = VEStringCreateFromBufferInternal(epdf->d_name);
      VELIST *node = VEListCreateInternal(0, fileName);
      VEListPutInternal(filesList, node);
    }
  }

  /* Close directory */
  closedir(dpdf);
#endif

  /* That's it */
  return filesList;
} // GetFilesInDirectory

/***
 * PURPOSE: Initialize resource manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEResourceInit( VEVOID )
{
  /* Already initialized */
  if (p_ResourceContainer)
    return TRUE;

  /* Resources container creation */
  p_ResourceContainer = VEInternalContainerCreate(VE_DEFAULT_CONTAINERSIZE, "Resources container");
  if (!p_ResourceContainer)
    return FALSE;

  /* That's it */
  return TRUE;
} /* End of 'VEResourceInit' function */

/***
 * PURPOSE: Delete resource data internally
 *   PARAM: [IN] resource - pointer to resource to unload
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEResourceDeleteInternal( VERESOURCE *resource )
{
  /* Wrong argument */
  if (!resource)
    return;

  VEStringDeleteInternal(resource->m_Name);
  VEStringDeleteInternal(resource->m_Path);
  VEStringDeleteInternal(resource->m_GroupName);
  Delete(resource);
} /* End of 'VEResourceDeleteInternal' function */

/***
 * PURPOSE: Deinitialize resource manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEResourceDeinit( VEVOID )
{
  if (!p_ResourceContainer)
    return;

  VEInternalContainerForeach((VEINTERNALCONTAINER*)p_ResourceContainer, (VEFUNCTION)VEResourceDeleteInternal);
  VEInternalContainerDelete((VEINTERNALCONTAINER*)p_ResourceContainer);
  p_ResourceContainer = NULL;
} /* End of 'VEResourceDeinit' function */

/***
 * PURPOSE: Read chunk from file
 *  RETURN: Readed chunk
 *   PARAM: [IN] f - file pointer to read chunk
 *  AUTHOR: Eliseev Dmitry
 ***/
VECHUNK VEChunkRead( FILE *f )
{
  VEUINT nameLength = 0;
  VECHUNK chunk;
  memset(&chunk, 0, sizeof(VECHUNK));

  /* Reading sizes */
  fread(&chunk.m_Size, 1, sizeof(VEULONG), f);
  fread(&nameLength, 1, sizeof(VEUINT), f);
  if (nameLength > 0)
  {
    chunk.m_Name = VEStringCreateInternal(nameLength+1);
    if (!chunk.m_Name)
      return chunk;

    /* Reading chunk name */
    fread(chunk.m_Name->m_Data, 1, nameLength, f);
    chunk.m_Name->m_Length = nameLength;
  }

  /* That's it */
  return chunk;
} /* End of 'VEChunkRead' function */

/***
 * PURPOSE: Determine resource type by it's file name
 *  RETURN: Resource type (on of VE_RESOURCE_XXX definitions)
 *   PARAM: [IN] name - resource file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBYTE VEResourceTypeObtain( const VESTRING *name )
{
  if (!name)
    return VE_RESOURCE_UNKNOWN;

  /* Check resource file name extension */
  if (VEBufferIsEndsWith(name->m_Data, VE_RESOURCEEXT_TEXTURE1))
    return VE_RESOURCE_TEXTURE;
  else if (VEBufferIsEndsWith(name->m_Data, VE_RESOURCEEXT_TEXTURE2))
    return VE_RESOURCE_TEXTURE;
  else if (VEBufferIsEndsWith(name->m_Data, VE_RESOURCEEXT_OBJECT))
    return VE_RESOURCE_OBJECT;
  else if (VEBufferIsEndsWith(name->m_Data, VE_RESOURCEEXT_SCENE))
    return VE_RESOURCE_SCENE;
  else if (VEBufferIsEndsWith(name->m_Data, VE_RESOURCEEXT_SKIN))
    return VE_RESOURCE_SKIN;
  else if (VEBufferIsEndsWith(name->m_Data, VE_RESOURCEEXT_DIALOG))
    return VE_RESOURCE_DIALOG;
  else if (VEBufferIsEndsWith(name->m_Data, VE_RESOURCEEXT_MEDIA))
    return VE_RESOURCE_MEDIA;

  /* That's it */
  return VE_RESOURCE_UNKNOWN;
} /* End of 'VEResourceObtainType' function */

/***
 * PURPOSE: Normalize resource name (remove path and extension)
 *   PARAM: [IN/OUT] name - resource file name -> resource name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEResourceNameFix( VESTRING *name )
{
  VEINT pos = 0, lastSlashPos = 0;

  /* Wrong arguments */
  if (!name)
    return;

  /* Determine last slash position */
  for (pos = 0; pos < name->m_Size; pos++)
    if ((name->m_Data[pos] == '\\')||(name->m_Data[pos] == '/'))
      lastSlashPos = pos;

  /* Copying data */
  if (lastSlashPos > 0)
  {
    for (pos = lastSlashPos+1; pos < name->m_Size; pos++)
      name->m_Data[pos-lastSlashPos-1] = name->m_Data[pos];
    while (pos < name->m_Size)
      name->m_Data[pos++] = 0;
    name->m_Length -= lastSlashPos+1;
  }

  /* Cut an extension */
 lastSlashPos = -1;
  name->m_Length = VEBufferLength(name->m_Data);
  for (pos = name->m_Length; (pos > 0)&&(lastSlashPos == -1); pos--)
    if (name->m_Data[pos] == '.')
      lastSlashPos = pos;

  /* Cut an extension */
  if (lastSlashPos != -1)
  {
    pos = lastSlashPos;
    while (pos < name->m_Size)
      name->m_Data[pos++] = 0;
    name->m_Length = lastSlashPos;
  }
} /* End of 'VEResourceNameFix' function */

/***
 * PURPOSE: Register resource file on engine
 *   PARAM: [IN] name     - resource group name
 *   PARAM: [IN] filename - resource file name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEResourceRegisterArchive( const VEBUFFER name, const VEBUFFER filename )
{
  FILE *resFile = NULL;
  VECHUNK chunk;
  VEHEADER header;

  /* Wrong arguments */
  if (!(name&&filename))
    return FALSE;

  /* Module is not initialized */
  if (!p_ResourceContainer)
    return FALSE;

  /* Open resource file if exists */
  resFile = fopen(name, "rb");
  if (!resFile)
    return FALSE;

  /* File format checking */
  fread(&header, 1, sizeof(VEHEADER), resFile);
  if (!VEHeaderIsValid(header, VE_FORMAT_RESOURCE))
  {
    fclose(resFile);
    return FALSE;
  }

  /* Reading resources */
  while (!feof(resFile))
  {
    /* Process chunk */
    chunk = VEChunkRead(resFile);
    if (chunk.m_Size != 0)
    {
      VERESOURCE *resource = New(sizeof(VERESOURCE), "Resource");
      if (!resource)
      {
        fclose(resFile);
        return FALSE;
      }

      /* It's an archive */
      resource->m_IsArchive = TRUE;

      /* Store group name and archive file name */
      resource->m_GroupName = VEStringCreateFromBufferInternal(name);
      resource->m_Path = VEStringCreateFromBufferInternal(filename);

      /* Storing offset and resource size */
      resource->m_Size   = chunk.m_Size;
      resource->m_Offset = ftell(resFile);

      /* Determine resource type */
      resource->m_Type = VEResourceTypeObtain(chunk.m_Name);

      /* Store resource name */
      resource->m_Name = chunk.m_Name;
      VEResourceNameFix(resource->m_Name);

      /* Add resource to container */
      VEInternalContainerAdd((VEINTERNALCONTAINER*)p_ResourceContainer, resource);

      /* Skip chunk's data */
      fseek(resFile, chunk.m_Size, SEEK_CUR);
    }
  }

  /* Close archive file */
  fclose(resFile);

  /* Output information */
  VEConsolePrintArg("Resource file registered: ", filename);

  /* Adding resource file to container */
  return TRUE;
} /* End of 'VEResourceRegisterArchive' function */

/***
 * PURPOSE: Register resources path
 *   PARAM: [IN] name - resource group name
 *   PARAM: [IN] path - relative path to resources on HDD (root is working directory)
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEResourceRegisterPath( const VEBUFFER name, const VEBUFFER path )
{
  VELIST *cur = NULL;
  VELIST *files = VEDirectoryList(path);
  if (!files)
    return FALSE;

  cur = files->m_Next;
  while (cur)
  {
    VERESOURCE *resource = NULL;
    VESTRING *fileName = (VESTRING*)cur->m_Data;
    if (!fileName)
      break;
    resource = New(sizeof(VERESOURCE), "Resource");
    if (!resource)
      break;

    /* Fix resource name */
    resource->m_GroupName = VEStringCreateFromBufferInternal(name);
    resource->m_IsArchive = FALSE;
    resource->m_Path = VEPathBuild(path, fileName->m_Data);
    resource->m_Name = fileName;
    resource->m_Type = VEResourceTypeObtain(resource->m_Name);
    VEResourceNameFix(resource->m_Name);

    /* Add resource to container */
    VEInternalContainerAdd((VEINTERNALCONTAINER*)p_ResourceContainer, resource);
    cur = cur->m_Next;
  }

  { /* Output information */
    VECHAR message[VE_BUFFER_STANDART];
    sprintf(message, "Resource path %s registered as %s", path, name);
    VEConsolePrint(message);
  }

  /* Release memory */
  VEListDeleteInternal(files);
  return TRUE;
} /* End of 'VEResourceRegisterPath' function */

/***
 * PURPOSE: Unload resource group
 *   PARAM: [IN] name - resource group name
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEResourceUnregister( const VEBUFFER name )
{
  VEUINT itemID = 0;

  /* Manager is not initialized */
  if (!p_ResourceContainer)
    return;

  VESectionEnterInternal(p_ResourceContainer->m_Sync);
  for (itemID = 0; itemID < p_ResourceContainer->m_Size; itemID++)
    if (p_ResourceContainer->m_Items[itemID])
    {
      VERESOURCE *resource = p_ResourceContainer->m_Items[itemID];

      /* Comparing resource group names */
      if (resource->m_GroupName)
        if (VEBuffersEqual(resource->m_GroupName->m_Data, name))
        {
          p_ResourceContainer->m_Items[itemID] = NULL;
          p_ResourceContainer->m_NumItems--;

          /* Delete resource */
          VEResourceDeleteInternal(resource);
        }
    }
  VESectionLeaveInternal(p_ResourceContainer->m_Sync);

  /* Output information */
  VEConsolePrintArg("Resource unregistered: ", name);
} /* End of 'VEResourceUnregister' function */

/***
 * PURPOSE: Get resource by it's name
 *  RETURN: Pointer to resource if success, NULL otherwise
 *   PARAM: [IN] resourceName - name of resource
 *  AUTHOR: Eliseev Dmitry
 ***/
VERESOURCE *VEResourceGet( const VEBUFFER resourceName )
{
  VERESOURCE *res = NULL;
  VEUINT itemID = 0;

  /* Manager is not initialized */
  if (!p_ResourceContainer)
    return NULL;

  VESectionEnterInternal(p_ResourceContainer->m_Sync);
  for (itemID = 0; (itemID < p_ResourceContainer->m_Size)&&(!res); itemID++)
    if (p_ResourceContainer->m_Items[itemID])
    {
      VERESOURCE *tempResource = p_ResourceContainer->m_Items[itemID];

      /* Are resource found */
      if (tempResource->m_Name)
        if (VEBuffersEqual(tempResource->m_Name->m_Data, resourceName))
          res = tempResource;
    }
  VESectionLeaveInternal(p_ResourceContainer->m_Sync);

  /* That's it */
  return res;
} /* End of 'VEResourceGet' function */

/***
 * PURPOSE: Open resource file. At first try to open file on HDD, at second try to search it in archive
 *  RETURN: Pointer to opened file if success, NULL otherwise
 *   PARAM: [IN] resourceName - name of resource
 *  AUTHOR: Eliseev Dmitry
 ***/
VEFILE *VEResourcePtrOpen( const VEBUFFER resourceName )
{
  VERESOURCE *resource = NULL;
  VESTRING *fixedName = NULL;

  /* Try to open HDD file */
  VEFILE *resFile = VEFileOpenInternal(resourceName);
  if (resFile)
    return resFile;

  /* Try to fix name  */
  fixedName = VEStringCreateFromBufferInternal(resourceName);
  if (!fixedName)
    return 0;
  VEResourceNameFix(fixedName);

  /* Try to find resource in registered resources */
  resource = VEResourceGet(fixedName->m_Data);
  VEStringDeleteInternal(fixedName);
  if (!resource)
  {
    VEConsolePrintArg("Can't find resource file", resourceName);
    return NULL;
  }

  /* Try to open resource file or registered HDD file */
  resFile = VEFileOpenInternal(resource->m_Path->m_Data);
  if (!resFile)
    return NULL;

  /* Move to resource beginning */
  fseek(resFile->m_Ptr, resource->m_Offset, SEEK_SET);
  return resFile;
} /* End of 'VEResourcePtrOpen' function */

/***
 * PURPOSE: Close opened resource file
 *   PARAM: [IN] resFile - pointer to opened file
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEResourcePtrClose( VEFILE *resFile )
{
  VEFileCloseInternal(resFile);
} /* End of 'VEResourcePtrClose' function */

/***
 * PURPOSE: Print resource information to console
 *   PARAM: [IN] resource - resource to print information
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEResourcePrint( VERESOURCE *resource )
{
  VECHAR resourceInfo[VE_BUFFER_LARGE];
  memset(resourceInfo, 0, VE_BUFFER_LARGE);

  /* Print scene information to console */
  sprintf(resourceInfo, "%s: %s", resource->m_GroupName->m_Data, resource->m_Name->m_Data);
  VEConsolePrint(resourceInfo);
} /* End of 'VEResourcePrint' function */

/***
 * PURPOSE: Print resource list to console
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEResourcesList( VEVOID )
{
  VEInternalContainerForeach((VEINTERNALCONTAINER*)p_ResourceContainer, (VEFUNCTION)VEResourcePrint);
} /* End of 'VEResourcesList' function */
