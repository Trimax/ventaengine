# Venta Engine
## TODO list
1. Console
2. Texture manager
   1. AMBIENT_OCCLUSION, 
   2. ANISOTROPY, 
   3. ~~DIFFUSE~~, 
   4. ~~HEIGHT~~, 
   5. METALLIC, 
   6. NORMAL, 
   7. ROUGHNESS;
3. Normal mapping
4. Environment: box/sphere (add box to the scene)
5. Emitters (extend the scene with a list of emitters)
6. Resource archive manager and also, pack & browse tool (!)
7. Load gizmo by default. Set visibility to false

## Important
On macOS start with VM argument: `-XstartOnFirstThread`

## Notes
### MacOS executable
```
jpackage \
--input examples/target \
--main-jar examples-0.0.21.jar \
--main-class com.venta.examples.CubeApplication \
--name CubeApp \
--app-version 1.0.21 \
--icon engine/src/main/resources/icons/app.icns \
--type dmg
```