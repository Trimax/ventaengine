# Venta Engine
## TODO list
1. ~~Figure out window order initialization~~
2. Add possibility to link program to an object dynamically
3. Move the cube code from engine to example
4. Create separate classes for parsed objects and in-memory objects
5. Create scene manager and possibility to add objects to scene
6. Create camera manager
7. Console
8. Deinitialization of managers, resources cleanup
9. Adjust window on resize
10. Fullscreen mode
11. Calculate faces' normals

## Notes
1. Scene contains list of objects
2. Object should be assigned to a shader (or opposite)
3. Scene render: for each object: pick shader -> render

On macOS start with VM argument: -XstartOnFirstThread