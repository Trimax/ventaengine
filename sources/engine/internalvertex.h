#ifndef INTERNALVERTEX_H_INCLUDED
#define INTERNALVERTEX_H_INCLUDED

#include "vector2d.h"
#include "vector3d.h"
#include "vector4d.h"

/* Vertex definition */
typedef struct tagVEVERTEX
{
  VEVECTOR3D m_Position;
  VEVECTOR3D m_Normal;
  VEVECTOR2D m_TCoordionates;
  VEVECTOR4D m_Color;
  VEVECTOR4D m_Tangent;
  VEVECTOR3D m_Binormal;
} VEVERTEX;

#endif // INTERNALVERTEX_H_INCLUDED
