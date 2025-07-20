# Venta Engine
## TODO list
1. Move MVP matrix computation to CPU (to avoid multiple computations per vertex)
2. Deinitialization of managers, resources cleanup
3. Fullscreen mode
4. Calculate faces' normals
5. Console

## Notes
1. Scene contains list of objects
2. Object should be assigned to a shader (or opposite)
3. Scene render: for each object: pick shader -> render

## Important
On macOS start with VM argument: -XstartOnFirstThread