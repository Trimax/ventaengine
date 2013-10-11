@ECHO OFF
CLS

REM Prepare directories
MKDIR bin\debug
MKDIR bin\release

REM Copying DLLs
COPY /Y 3rdparty\dll\*.dll bin\debug\
COPY /Y 3rdparty\dll\*.dll bin\release\