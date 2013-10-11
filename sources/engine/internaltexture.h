#ifndef INTERNALTEXTURE_H_INCLUDED
#define INTERNALTEXTURE_H_INCLUDED

#include "internalheader.h"
#include "types.h"

/* VET format header */
typedef struct tagVEHEADERVET
{
  VEHEADER m_Header; /* Common header */
  VEUINT   m_Width;  /* Texture width */
  VEUINT   m_Height; /* Texture height */
} VEHEADERVET;

#endif // INTERNALTEXTURE_H_INCLUDED
