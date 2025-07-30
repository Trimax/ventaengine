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

**VentaEngine** is a lightweight, modular 3D engine written in Java using OpenGL bindings. It is designed with flexibility and 
performance in mind, aiming to provide a solid foundation for building interactive 3D applications and games.
Currently, the engine is in active development. Many core systems — including scene management, rendering pipeline, 
shader abstraction, and input handling — are being built and refined. While a functional base is in place, the engine 
is not yet feature-complete or production-ready.
We welcome contributions from the community! Whether you want to help improve the rendering system, add new features, 
fix bugs, or optimize performance — your input and code are highly appreciated. Feel free to fork the repository, open 
issues, or submit pull requests. Join us in building a powerful, extensible 3D engine tailored for Java developers!


## Features
* Window management
* Scene management with support for multiple objects, emitters and lights
* Flexible material and texture system
* Shader program abstraction (vertex & fragment shaders)
* Camera control with perspective and orthographic projections
* Efficient rendering pipeline with support for reusable mesh entities
* Debug rendering tools, including gizmos for lights and objects
* Hierarchical scene graph support for complex models and transformations


## Architecture Overview
### Core Entities
- **Window** — Represents the application window and OpenGL context management. Handles user input and viewport setup.
- **Camera** — Defines the viewpoint in the 3D world, supporting perspective and orthographic projections, view matrices, and movement controls.
- **Scene** — Contains collections of Objects and Lights, managing the overall composition of the 3D world.
- **Object** — An entity in the scene that references a Mesh and holds transformation data (position, rotation, scale, model matrix).
- **Mesh** — Stores the geometry data (vertices, indices, buffers) that define the shape of an Object; designed for reuse across multiple Objects.
- **Light** — Represents a light source in the scene; affects shading but may not be rendered itself.
- **Shader** — Encapsulates individual shader stages (e.g., vertex, fragment shaders).
- **Program** — Combines Shader stages into a complete GPU program used for rendering.

### Controllers and Rendering Components
- **Manager** — Responsible for lifecycle management of engine entities (creation, deletion, resource tracking). Each core entity typically has its own Manager (e.g., ObjectManager, MeshManager).
- **Binder** — Handles binding of uniforms and GPU resources (textures, buffers) to shader programs before rendering.
- **Renderer** — Performs the actual drawing of entities to the screen, coordinating with Managers and Binders to render scenes efficiently.

## Getting Started
The engine requires Java 22+ and OpenGL 3.3+. To build and run, use your preferred Java IDE or build system.

On macOS start with VM argument: `-XstartOnFirstThread`

## Contribution

Contributions, issues, and feature requests are welcome! Feel free to fork the repository and submit pull requests.

## Roadmap
The roadmap is reflected in the GitHub issues section. Long story short:

1. Introduce mesh manager etc
   1. ~~Make the camera a part of scene (Scene should contain a list of cameras)~~
   2. Render pyramid for camera in debug mode
   3. Render bounding boxes for objects
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

## Other (notes)
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