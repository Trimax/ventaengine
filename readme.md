# Venta Engine
## TODO list
1. ~~Figure out window order initialization~~
2. ~~Add possibility to link program to an object dynamically~~
3. ~~Move the cube code from engine to example~~
4. Create separate classes for parsed objects and in-memory objects
5. Create scene manager and possibility to add objects to scene
6. ~~Move renderers to a separate class~~
7. Create camera manager
8. Console
9. Deinitialization of managers, resources cleanup
10. Adjust window on resize
11. Fullscreen mode
12. Calculate faces' normals

## Notes
1. Scene contains list of objects
2. Object should be assigned to a shader (or opposite)
3. Scene render: for each object: pick shader -> render

On macOS start with VM argument: -XstartOnFirstThread