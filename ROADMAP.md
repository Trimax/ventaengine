# Roadmap

What is left to do, in the order it is planned: every step builds on the previous ones and makes a visible difference on
its own. What the engine can do already is in the [readme](readme.md). Paths of shaders are relative to
`engine/src/main/resources/shaders/`.

## Rendering

- [ ] **1. Reflections of the other surfaces.** Reflective materials (`default/fragment.glsl`) reflect only the skybox
  cubemap, so objects, terrain and other surfaces never appear in their reflections (the water has its own reflection of the
  scene). A dynamic cubemap of the surroundings of a reflective object (drawn from its position, refreshed now and then)
  would fix it. The reflection of the water has no particles and billboards in it, and only the nearest water of a scene has it.
- [ ] **2. Antialiasing of the cutout edges.** The edges of the holes of the cutout materials (`"alphaMode": "cutout"`) are
  not smoothed by the multisampling, which only sees the edges of the geometry. Alpha to coverage would smooth them.

## Animation

- [ ] **3. Animation.** Objects move only from code: there are no animations in the resources. Keyframe
  animation of the transforms of the hierarchy nodes (position, rotation, scale with interpolation) is the first
  step, and the boxes of the nodes must follow them, or the culling would cut a moving object; skeletal animation
  (skinned meshes with joints and weights, played and blended in the vertex shader) needs a format with skeletons, so
  it comes with a glTF loader, which also brings PBR materials and scenes in one file.

## Later

- [ ] **4. Temporal antialiasing.** The image with the post effects shimmers on thin things (leaves, wires) in motion,
  which FXAA only softens. TAA keeps the history of the frames and needs the motion vectors of what moves: the camera,
  the objects and the skinned meshes, so it comes after the animation.
- [ ] **5. Volumetric clouds.** The clouds are painted on the 8 bit skybox: they don't move, they cast no shadows on the
  land and no light shafts, and the sun can't be hidden by one. Clouds made of a noise (a march through a layer of
  the sky, or a few layers of cards), lit by the sun and the sky, would drift, shadow the land through the shadow maps
  and the light shafts, and change the light of the scene as they pass.
- [ ] **6. Ambient occlusion of the indirect light only.** The ambient occlusion multiplies the finished image, so it
  darkens the direct light of the sun in a corner as well, where the shadow maps decide what is lit. The shaders of
  the surfaces should read it and apply it to the light of the sky and to the image based lighting only. For that
  the occlusion must be known before the surfaces are drawn: a depth pass over the objects and the terrain (with the
  wind, the holes of the cutouts and the instancing), the occlusion from its depth, and then the pass of the colors with
  the depth test that accepts what the depth pass wrote (the vertices must give the same depths in both).

## Editor

- [ ] **7. Editor redesign.** The editor (`editor/`) uses the default light Modena look with a couple of tiny style
  sheets and PNG icons, and its center is a placeholder: `layouts/main.fxml` has a black `VBox` instead of a scene
  view, the editor never starts the engine and the camera buttons of the toolbar have no actions. It should be
  rebuilt like the map editor of The Native Land: a dark theme in one style sheet (a palette of looked-up colors,
  spacing and font size scales, style classes for forms, panels, the status bar and the toolbar), panels in FXML
  included into a `BorderPane` (a toolbar with SVG icons and indicators, the resource tree on the left, the scene
  view in the center, a property inspector on the right, a status bar), controllers bound to shared contexts with
  JavaFX properties, undo and redo, keyboard shortcuts. The scene view renders the engine offscreen (a hidden window
  of the view size, resized with it) and shows the tone mapped frame: the pixels of the default framebuffer are read
  after the post processing and before the buffer swap, on the engine thread (GLFW needs the main thread on macOS,
  which JavaFX uses too), flipped and converted to BGRA for a JavaFX `PixelBuffer`; the camera is controlled with the
  mouse and the toolbar buttons, objects can be selected and moved with gizmos.
