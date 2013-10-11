#ifndef TYPES_H_INCLUDED
#define TYPES_H_INCLUDED

/*** Engine version ***/
#define VE_VERSION "1.0"

/*** Define engine types ***/

/* Boolean [TRUE/FALSE] */
typedef unsigned char VEBOOL;

/* Byte [0-255] */
typedef unsigned char VEBYTE;

/* Character [-128; 127] */
typedef char VECHAR;

/* Short [-32768; 32767] */
typedef short int VESHORT;

/* Unsigned short [0; 65535] */
typedef unsigned short int VEUSHORT;

/* Integer [-2147483648; 2147483647] */
typedef int VEINT;

/* Unsigned integer [0; 4294967295] */
typedef unsigned int VEUINT;

/* Long [-2147483648; 2147483647] */
typedef long VELONG;

/* Unsigned long [0; 4294967295] */
typedef unsigned long VEULONG;

/* Real [-1.7e308*; 1.7e308] */
typedef float VEREAL;

/* Nothing (empty type) */
typedef void VEVOID;

/* Pointer */
typedef void* VEPOINTER;

/* Buffer */
typedef char* VEBUFFER;


/*** Bool type values ***/
#define TRUE  1
#define FALSE 0

/*** Pointer values ***/
#ifndef NULL
#define NULL 0
#endif

/*** Buffers sizes ***/

/* Small buffer */
#define VE_BUFFER_SMALL      256

/* Standart buffer */
#define VE_BUFFER_STANDART   512

/* Large buffer */
#define VE_BUFFER_LARGE      1024

/* Extralarge buffer */
#define VE_BUFFER_EXTRALARGE 2048


/*** Functions ***/

/* Function without arguments */
typedef VEVOID (*VEFUNCVOIDVOID)(VEVOID);
typedef VEFUNCVOIDVOID VEPROCEDURE;

/* Simple function */
typedef VEVOID (*VEFUNCVOIDPTR)(VEPOINTER);
typedef VEFUNCVOIDPTR VEFUNCTION;

/* Simple function with two arguments */
typedef VEVOID (*VEFUNCVOIDPTRPTR)(VEPOINTER, VEPOINTER);
typedef VEFUNCVOIDPTRPTR VEFUNCTION2;

/* Processor function */
typedef VEPOINTER (*VEFUNCPTRPTR)(VEPOINTER);
typedef VEFUNCPTRPTR VEFUNCTIONWITHRESULT;

/* Filter function */
typedef VEBOOL (*VEFUNCBOOLPTR)(VEPOINTER);
typedef VEFUNCBOOLPTR VEFUNCTIONFILTER;

/* Console processor function */
typedef VEBOOL (*VEFUNCBOOLBUFFER)(VEBUFFER);
typedef VEFUNCBOOLBUFFER VEFUNCTIONCONSOLEPROCESSOR;

#endif // TYPES_H_INCLUDED
