# Venta Engine
## TODO list
1. Console
2. Environment: box/sphere (add box to the scene)
3. Emitters (extend the scene with a list of emitters)
4. Resource archive manager and also, pack & browse tool (!)
5. Render gizmo for light and pyramid for camera in debug mode

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