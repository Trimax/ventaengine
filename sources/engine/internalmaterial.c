#include "internalmaterial.h"
#include "texturemanager.h"
#include "internalstring.h"
#include "internalbuffer.h"
#include "memorymanager.h"
#include "console.h"
#include "math.h"

#include <string.h>
#include <stdlib.h>

/***
 * PURPOSE: Load material map
 *  RETURN: Texture id if success, 0 otherwise
 *   PARAM: [IN] f - pointer to file to read data
 *  AUTHOR: Eliseev Dmitry
 ***/
VEUINT VEMaterialMapLoad( FILE *f )
{
  VEUINT size = 0, mapID = 0;
  VESTRING *s = NULL;
  VEBYTE isMapDefined = FALSE;

  /* Read map flag */
  fread(&isMapDefined, 1, sizeof(VEBYTE), f);
  if (!isMapDefined)
    return 0;

  /* Read texture name and load texture */
  fread(&size, 1, sizeof(VEUINT), f);

  s = VEStringCreateInternal(size+1);
  fread(s->m_Data, 1, size, f);
  s->m_Length = size;

  { /* Preprocess file name. Cut path */
    VEINT lastSlashPos = VEBufferLastIndexOf(s->m_Data, '/'), lastBackslashPos = VEBufferLastIndexOf(s->m_Data, '\\'), pos = 0;
    VEINT slashPos = VEMAX(lastSlashPos, lastBackslashPos);
    if (slashPos > -1)
    {
      for (pos = slashPos+1; pos < s->m_Length; pos++)
        s->m_Data[pos-slashPos-1] = s->m_Data[pos];
      s->m_Length -= slashPos+1;
      memset(&s->m_Data[s->m_Length], 0, slashPos);
    }
  }

  /* Load texture */
  mapID = VETextureLoad(s->m_Data);
  if (!mapID)
    VEConsolePrintArg("Texture not found: ", s->m_Data);

  /* Skip \0 byte */
  fseek(f, 1, SEEK_CUR);

  VEStringDeleteInternal(s);

  return mapID;
} /* End of 'VEMaterialMapLoad' function */

/***
 * PURPOSE: Load material
 *  RETURN: Pointer to loaded material data if success, NULL otherwise
 *   PARAM: [IN] f - pointer to file to read data
 *  AUTHOR: Eliseev Dmitry
 ***/
VEMATERIAL *VEMaterialLoad( FILE *f )
{
  VEMATERIAL *material = New(sizeof(VEMATERIAL), "Material");
  if (!material)
    return NULL;

  /* Read material index */
  fread(&material->m_MaterialID, 1, sizeof(VEINT), f);

  /* Read material colors */
  fread(&material->m_Ambient,  1, sizeof(VECOLOR), f);
  fread(&material->m_Diffuse,  1, sizeof(VECOLOR), f);
  fread(&material->m_Specular, 1, sizeof(VECOLOR), f);

  /* Read self-illumination */
  fread(&material->m_SelfIllumination, 1, sizeof(VECOLOR), f);

  /* Read specular & glossiness */
  fread(&material->m_Specular,   1, sizeof(VEREAL), f);
  fread(&material->m_Glossiness, 1, sizeof(VEREAL), f);

  /* Reading maps */
  material->m_MapDiffuse    = VEMaterialMapLoad(f);
  material->m_MapOpacity    = VEMaterialMapLoad(f);
  material->m_MapNormals    = VEMaterialMapLoad(f);
  material->m_MapReflection = VEMaterialMapLoad(f);

  /* That's it */
  return material;
} /* End of 'VEMaterialLoad' function */
