# Roadmap

All the known issues found during the code review of 1.2.61 are fixed. The planned features are listed below in the
order they will be done: every step builds on the previous ones and makes a visible difference on its own.
The scene is rendered in HDR and tone mapped, so the post effects below render into its framebuffer, and the sun
casts cascaded shadows (point lights don't cast shadows yet: cubemap depth maps are the next step for them).
Paths are relative to `engine/src/main/resources/shaders/` unless stated otherwise.

## Rendering

- [ ] **1. Physically based lighting.** The shaders use Blinn-Phong like lighting, so materials look like plastic.
  A Cook-Torrance BRDF (GGX distribution, Smith geometry, Schlick fresnel) driven by the metalness and roughness of
  the materials, and image based lighting from the skybox (a diffuse irradiance cubemap and a specular cubemap
  prefiltered by roughness with a BRDF lookup table) give the objects the color of the sky around them.
- [ ] **2. Alpha cutout.** Materials can't cut holes by the texture alpha: `default/fragment.glsl` never discards
  fragments, and a semi-transparent texture is only alpha blended without sorting. Leaves, grass, fences and other
  foliage need an alpha threshold in the material (fragments below it are discarded, the rest are opaque and write
  depth) and double-sided faces for thin geometry like leaf cards.
- [ ] **3. Instancing and foliage.** Every object is a separate draw call, so a few hundred trees already cost a lot.
  Objects sharing a mesh and a material should be drawn with instanced rendering (a buffer of model matrices), which
  makes forests and grass possible; foliage also needs a wind sway in the vertex shader.
- [ ] **4. Levels of detail.** A far object costs as much as a close one. Objects should have several meshes
  (simpler with the distance, the last one can be a billboard with a picture of the object) and switch between them
  by the distance or the screen size, with a short crossfade (dithered) so the switch doesn't pop; terrain far away
  can use coarser grid cells as well.
- [ ] **5. Ambient occlusion.** Contact shadows in corners, under walls and around the roots of trees are missing, so
  objects look like floating on the ground. Screen space ambient occlusion (from the depth and the normals of the
  scene, blurred) darkens the ambient and the image based lighting there.
- [ ] **6. Atmosphere.** The fog is a layer with a sharp top, and bright light has no glow. An exponential height fog
  (the density decreases with the height, integrated along the view ray) with the sun color scattered towards the
  sun, light shafts (god rays) and bloom (bright parts of the HDR image blurred and added back) give the hazy,
  glowing look of sunsets and forests.
- [ ] **7. Terrain materials.** Terrain materials are picked by the height only, so rock never appears on steep
  slopes and textures stretch on cliffs. Materials should be blended by the height and the slope, sampled
  triplanarly on steep parts, with a detail normal map close to the camera.
- [ ] **8. Water and environment reflections.** Water (`water/fragment.glsl`) and reflective materials
  (`default/fragment.glsl`) reflect only the skybox cubemap, so objects, terrain and other surfaces never appear in
  reflections. Planar reflections for water (the scene rendered mirrored around the water plane into a texture) and
  optionally a dynamic cubemap for reflective objects would fix it; with the depth of the scene the water also gets
  its color by the depth, a foam line along the shore and refraction.
- [ ] **9. Antialiasing of the post effects.** Multisampling smooths the geometry edges only; with the post effects
  and alpha cutout foliage the image needs FXAA or temporal antialiasing (TAA, which needs motion vectors).

## Animation

- [ ] **10. Animation.** Objects move only from code: there are no animations in the resources. Keyframe
  animation of the transforms of the hierarchy nodes (position, rotation, scale with interpolation) is the first
  step; skeletal animation (skinned meshes with joints and weights, played and blended in the vertex shader) needs a
  format with skeletons, so it comes with a glTF loader, which also brings PBR materials and scenes in one file.

## Editor

- [ ] **11. Editor redesign.** The editor (`editor/`) uses the default light Modena look with a couple of tiny style
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
