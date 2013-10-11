#ifndef INTERNALRESOURCEMANAGER_H_INCLUDED
#define INTERNALRESOURCEMANAGER_H_INCLUDED

#include "internalcriticalsection.h"
#include "internalstring.h"
#include "internalarray.h"
#include "internalfs.h"
#include "types.h"

#include <stdio.h>

/*** Resource types ***/

/* Unknown resource type */
#define VE_RESOURCE_UNKNOWN 0

/* Dialog data .VED */
#define VE_RESOURCE_DIALOG 1
#define VE_RESOURCEEXT_DIALOG "ved"

/* Texture data .VET, .TGA */
#define VE_RESOURCE_TEXTURE 2
#define VE_RESOURCEEXT_TEXTURE1 "vet"
#define VE_RESOURCEEXT_TEXTURE2 "tga"

/* Skin data .VES */
#define VE_RESOURCE_SKIN 3
#define VE_RESOURCEEXT_SKIN "ves"

/* 3D object data .VEO */
#define VE_RESOURCE_OBJECT 4
#define VE_RESOURCEEXT_OBJECT "veo"

/* Scene data .VEC */
#define VE_RESOURCE_SCENE 5
#define VE_RESOURCEEXT_SCENE "vec"

/* Sound file data .VEM */
#define VE_RESOURCE_MEDIA 6
#define VE_RESOURCEEXT_MEDIA "vem"

/* Single resource */
typedef struct tagVERESOURCE
{
  VESTRING *m_GroupName; /* Resources group name */
  VESTRING *m_Name;      /* Resource name */
  VESTRING *m_Path;      /* Path to resource (either HDD path or name of archive file) */
  VEUINT    m_Offset;    /* Offset to resource beginning from the file start */
  VEUINT    m_Size;      /* Resource size */
  VEBOOL    m_IsArchive; /* Source type (TRUE if it's archive, FALSE if HDD path) */
  VEBYTE    m_Type;      /* Resource type */
} VERESOURCE;

/***
 * PURPOSE: Initialize resource manager
 *  RETURN: TRUE if success, FALSE otherwise
 *  AUTHOR: Eliseev Dmitry
 ***/
VEBOOL VEResourceInit( VEVOID );

/***
 * PURPOSE: Get resource by it's name
 *  RETURN: Pointer to resource if success, NULL otherwise
 *   PARAM: [IN] resourceName - name of resource
 *  AUTHOR: Eliseev Dmitry
 ***/
VERESOURCE *VEResourceGet( const VEBUFFER resourceName );

/***
 * PURPOSE: Open resource file. At first try to open file on HDD, at second try to search it in archive
 *  RETURN: Pointer to opened file if success, NULL otherwise
 *   PARAM: [IN] resourceName - name of resource
 *  AUTHOR: Eliseev Dmitry
 ***/
VEFILE *VEResourcePtrOpen( const VEBUFFER resourceName );

/***
 * PURPOSE: Close opened resource file
 *   PARAM: [IN] resFile - pointer to opened file
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEResourcePtrClose( VEFILE *resFile );

/***
 * PURPOSE: Print resource list to console
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEResourcesList( VEVOID );

/***
 * PURPOSE: Deinitialize resource manager
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VEResourceDeinit( VEVOID );

#endif // INTERNALRESOURCEMANAGER_H_INCLUDED
