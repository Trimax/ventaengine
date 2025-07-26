# Venta Engine
## TODO list
1. Render gizmo for light and pyramid for camera in debug mode
2. Console
3. Environment: box/sphere (add box to the scene)
4. Emitters (extend the scene with a list of emitters)
5. Resource archive manager and also, pack & browse tool (!)

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