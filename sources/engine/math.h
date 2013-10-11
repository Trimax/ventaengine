#ifndef MATH_H_INCLUDED
#define MATH_H_INCLUDED

#include "quaternion.h"
#include "vector4d.h"
#include "vector3d.h"
#include "vector2d.h"

/* Min macros */
#ifndef VEMIN
#define VEMIN(a,b) ((a)<(b)?(a):(b))
#endif

#ifndef VEMAX
#define VEMAX(a,b) ((a)>(b)?(a):(b))
#endif

#endif // MATH_H_INCLUDED
