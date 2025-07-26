```
░██    ░██                      ░██            ░██████████                    ░██
░██    ░██                      ░██            ░██
░██    ░██ ░███████ ░████████░████████░██████  ░██        ░████████  ░████████░██░████████  ░███████
░██    ░██░██    ░██░██    ░██  ░██        ░██ ░█████████ ░██    ░██░██    ░██░██░██    ░██░██    ░██
 ░██  ░██ ░█████████░██    ░██  ░██   ░███████ ░██        ░██    ░██░██    ░██░██░██    ░██░█████████
  ░██░██  ░██       ░██    ░██  ░██  ░██   ░██ ░██        ░██    ░██░██   ░███░██░██    ░██░██
   ░███    ░███████ ░██    ░██   ░████░█████░██░██████████░██    ░██ ░█████░██░██░██    ░██ ░███████
                                                                           ░██
                                                                     ░███████
```


## Roadmap
1. Introduce mesh manager etc
   1. Save meshes separately from objects. Object uses mesh hierarchy
   2. Introduce debug visuals. Like objects, but for debug purposes
   3. Render gizmo for light and pyramid for camera in debug mode
   4. Bounding boxes
2. Console
3. Environment: box/sphere (add box to the scene)
4. Emitters (extend the scene with a list of emitters)
5. Resource archive manager and also, pack & browse tool (!)
   1. Separate module with UI. Works with archive definition, packs data
   2. Add archive support reader (to manager)
   3. Probably should use chunks and index
6. Create more example apps
7. Audio
8. Physics

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