#ifndef INTERNALGLUT_H_INCLUDED
#define INTERNALGLUT_H_INCLUDED

/* Disable "at exit" hack */
//#define GLUT_DISABLE_ATEXIT_HACK
#define GL_GLEXT_PROTOTYPES

/* Glut & OpenGL */
#ifdef _WIN32

#include <gl/glew.h>
#include <gl/gl.h>
#include <gl/glu.h>
#include "freeglut/freeglut.h"
//#include <gl/glut.h>

#else

#include <gl/gl.h>
#include "freeglut/freeglut.h"
//#include <gl/glut.h>
#include <gl/glext.h>
#include <GL/glu.h>

#endif


#endif // INTERNALGLUT_H_INCLUDED
