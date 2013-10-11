#include "internaltexturemanager.h"
#include "internaltexture.h"
#include "memorymanager.h"

#include <string.h>

/* Header of TGA file */
typedef struct tagVETEXTURE_TGA
{
  VEBYTE   m_Reserved;
  VEBYTE   m_ColorMap;
  VEBYTE   m_ImageType;
  VEUSHORT m_MapStart;
  VEUSHORT m_MapLength;
  VEBYTE   m_MapDepth;
  VEUSHORT m_XOffset;
  VEUSHORT m_YOffset;
  VEUSHORT m_Width;
  VEUSHORT m_Height;
  VEBYTE   m_BitsPerPixel;
  VEBYTE   m_ImageDescriptor;
} VETEXTURE_TGA;

/***
 * PURPOSE: Load texture from TGA format
 *  RETURN: Pointer to loaded texture data if success, NULL otherwise
 *   PARAM: [IN] f - pointer to file to read data
 *  AUTHOR: Eliseev Dmitry
 ***/
VETEXTURE *VETextureLoadTGA( FILE *f )
{
  VEBYTE *tempData = NULL;
  VEUINT bytesPerPixel = 0;
  VETEXTURE_TGA header;
  VETEXTURE *texture = New(sizeof(VETEXTURE), "TGA texture");
  if (!texture)
    return NULL;

  /* Reading file header */
  memset(&header, 0, sizeof(VETEXTURE_TGA));
  if (!fread(&header, 1, sizeof(VETEXTURE_TGA), f))
  {
    Delete(texture);
    return NULL;
  }

  /* Saving texture resolution */
  texture->m_Width = header.m_Width;
  texture->m_Height = header.m_Height;

  /* Determine color depth */
  bytesPerPixel = header.m_BitsPerPixel >> 3;
  if ((bytesPerPixel < 3)||(bytesPerPixel > 4))
  {
    Delete(texture);
    return NULL;
  }

  /* Temporary pixels buffer allocation */
  tempData = New(bytesPerPixel * texture->m_Width * texture->m_Height, "Temporary TGA texture data array");
  if (!tempData)
  {
    Delete(texture);
    return NULL;
  }

  /* Reading TGA data */
  if (!fread(tempData, 1, bytesPerPixel * texture->m_Width * texture->m_Height, f))
  {
    Delete(tempData);
    Delete(texture);
    return NULL;
  }

  /* Allocating memory for texture data in RGBA format (4 bytes per pixel) */
  texture->m_Data = (VEBYTE*)New((VEULONG)(4 * texture->m_Width * texture->m_Height), "TGA texture data");
  if (!texture->m_Data)
  {
    Delete(tempData);
    Delete(texture);
    return NULL;
  }

  /* Copying data */
  if (bytesPerPixel == 4)
    memcpy(texture->m_Data, tempData, 4 * texture->m_Width * texture->m_Height);
  else
  {
    VEUINT i = 0;
    for (i = 0; i < texture->m_Width * texture->m_Height; i++)
    {
      texture->m_Data[4*i]     = tempData[3*i + 0];
      texture->m_Data[4*i + 1] = tempData[3*i + 1];
      texture->m_Data[4*i + 2] = tempData[3*i + 2];
      texture->m_Data[4*i + 3] = 255;               /* Non-transparent alpha channel */
    }
  }

  /* That's it */
  Delete(tempData);
  return texture;
} /* End of 'VETextureLoadTGA' function */

/***
 * PURPOSE: Load texture from VET format
 *  RETURN: Pointer to loaded texture data if success, NULL otherwise
 *   PARAM: [IN] f - pointer to file to read data
 *  AUTHOR: Eliseev Dmitry
 ***/
VETEXTURE *VETextureLoadVET( FILE *f )
{
  VEHEADERVET header;
  VETEXTURE *texture = New(sizeof(VETEXTURE), "VET texture");
  if (!texture)
    return NULL;

  /* Reading file header */
  if (!fread(&header, 1, sizeof(VEHEADERVET), f))
  {
    Delete(texture);
    return NULL;
  }

  /* Is Venta file */
  if (!(VEHeaderIsValid(header.m_Header, VE_FORMAT_TEXTURE)||VEHeaderIsValid(header.m_Header, VE_FORMAT_TEXTURECOMPRESSED)))
  {
    Delete(texture);
    return NULL;
  }

  /* Store texture parameters */
  texture->m_Width  = header.m_Width;
  texture->m_Height = header.m_Height;

  /* Allocating memory for texture data in RGBA format (4 bytes per pixel) */
  texture->m_Data = (VEBYTE*)New(4 * header.m_Width * header.m_Height, "VET texture data");
  if (!texture->m_Data)
  {
    Delete(texture);
    return NULL;
  }

  /* Reading VET data */
  if (header.m_Header.m_Format == VE_FORMAT_TEXTURE)
  {
    if (!fread(texture->m_Data, 1, 4 * header.m_Width * header.m_Height, f))
    {
      Delete(texture->m_Data);
      Delete(texture);
      return NULL;
    }
  } else
  { /* Reading compressed VET data */
    VEUINT counter = 0, pos = 0, i = 0;
    VEBYTE symbol = 0;

    while (!feof(f))
    {
      fread(&counter, 1, sizeof(VEUINT), f);
      fread(&symbol,  1, sizeof(VEBYTE), f);

      for (i = 0; i < counter; i++)
        texture->m_Data[pos++] = symbol;
    }
  }

  /* That's it */
  return texture;
} /* End of 'VETextureLoadVET' function */

/***
 * PURPOSE: Unload texture
 *   PARAM: [IN] texture - pointer to loaded texture
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETextureUnload( VETEXTURE *texture )
{
  if (!texture)
    return;

  if (texture->m_Data)
    Delete(texture->m_Data);
  Delete(texture);
} /* End of 'VETextureUnload' function */

/***
 * PURPOSE: Save texture to compressed VET format
 *   PARAM: [IN] out     - file pointer to store texture
 *   PARAM: [IN] texture - pointer to loaded texture
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETextureSaveCompressed( FILE *out, const VETEXTURE *texture )
{
  VEUINT length = 4 * texture->m_Width * texture->m_Height, pos = 0, i = 0;

  /* For each data */
  for (pos = 0; pos < length; pos++)
  {
    VEBYTE symbol = texture->m_Data[pos];
    VEUINT counter = 1;

    /* Count serie length */
    for (i = pos; i < length; i++)
    {
      if (texture->m_Data[i+1] != symbol)
        break;
      counter++;
      pos++;
    }

    /* Write group */
    fwrite(&counter, 1, sizeof(VEUINT), out);
    fwrite(&symbol, 1, sizeof(VEBYTE), out);
  }
} /* End of 'VETextureSaveCompressed' function */

/***
 * PURPOSE: Save texture to VET format
 *   PARAM: [IN] filename   - file to store texture
 *   PARAM: [IN] texture    - pointer to loaded texture
 *   PARAM: [IN] compressed - flag to compress texture
 *  AUTHOR: Eliseev Dmitry
 ***/
VEVOID VETextureSave( const VEBUFFER filename, const VETEXTURE *texture, const VEBOOL compressed )
{
  VEHEADERVET header;
  FILE *out = NULL;

  /* Wrong pointers */
  if (!(filename && texture))
    return;

  /* Open file to write */
  out = fopen(filename, "wb");

  /* Prepare header */
  memset(&header, 0, sizeof(VEHEADERVET));
  header.m_Header.m_Prefix  = 'V';
  header.m_Header.m_Format = (compressed)?(VE_FORMAT_TEXTURE):(VE_FORMAT_TEXTURE);
  header.m_Header.m_Version = VE_FORMATVERSION;

  /* Set width & height */
  header.m_Width  = texture->m_Width;
  header.m_Height = texture->m_Height;

  /* Write header */
  fwrite(&header, 1, sizeof(VEHEADERVET), out);

  /* Write data */
  if (compressed)
    VETextureSaveCompressed(out, texture);
  else
    fwrite(texture->m_Data, 1, texture->m_Width * texture->m_Height * 4, out);

  /* That's it */
  fclose(out);
} /* End of 'VETextureSave' function */
