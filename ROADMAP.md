# Roadmap

All the known issues found during the code review of 1.2.61 are fixed. The planned features are listed below in the
order they will be done: every step builds on the previous ones and makes a visible difference on its own.
The scene is rendered in HDR and tone mapped, so the post effects below render into its framebuffer, and the sun
casts cascaded shadows (point lights don't cast shadows yet: cubemap depth maps are the next step for them).
Surfaces are shaded physically (a Cook-Torrance BRDF) and lit by their skybox (image based lighting); the skybox
is 8 bit, so a loader of HDR environment maps (`.hdr`, `.exr`) would give the sun and the bright sky their real brightness.
Materials can cut holes by the alpha of their texture (`"alphaMode": "cutout"`) and cast shadows with the holes; the edges of
the holes are not antialiased by the multisampling yet (alpha to coverage would smooth them).
Objects with cutout and opaque surfaces (`"alphaMode": "opaque"`; the default is `"blend"`, and such objects mix colors, so they are
sorted from the farthest to the nearest and drawn one by one) are drawn with instancing (the copies of a mesh that share a material
are one draw call, in the shadow maps as well); what is out of the view of the camera, or out of the box of a shadow cascade, is
not drawn at all. An object can have simpler models that replace its main model when it is small on the screen (`"lods"`, cross fading by a dither, the last level can be a card with a picture); the levels of terrain are not made yet. Bright parts of the image glow (`"bloom"` of the scene) and the sun can be a disc in the sky that glows with it (`"discSize"` of the directional light). A material that follows the wind (`"windInfluence"`, the wind of the scene is `"wind"`) sways with it.
Paths are relative to `engine/src/main/resources/shaders/` unless stated otherwise.

## Rendering

- [ ] **1. Atmosphere.** The fog is a layer with a sharp top. An exponential height fog (the density decreases with
  the height, integrated along the view ray) with the sun color scattered towards the sun, and light shafts (god rays:
  a march along the view ray that tests the points against the shadow maps of the sun, in the fog) give the hazy look
  of sunsets and forests. The light shafts need the depth of the scene as a texture (the scene is multisampled, so its
  depth must be resolved first).
- [ ] **2. Ambient occlusion.** Contact shadows in corners, under walls and around the roots of trees are missing, so
  objects look like floating on the ground. Screen space ambient occlusion (from the depth and the normals of the
  scene, blurred) darkens the ambient and the image based lighting there.
- [ ] **3. Terrain chunks and levels of detail.** A terrain is one grid that is drawn whole with one call, so far away
  it costs as much as close and every vertex of it is displaced by the heightmap. It should be cut into chunks that
  are culled by the view and drawn with coarser grid cells with the distance (the borders of chunks of different
  levels must not crack, for example with skirts or by morphing the vertices on the border).
- [ ] **4. Terrain materials.** Terrain materials are picked by the height only, so rock never appears on steep
  slopes and textures stretch on cliffs. Materials should be blended by the height and the slope, sampled
  triplanarly on steep parts, with a detail normal map close to the camera.
- [ ] **5. Water and environment reflections.** Water (`water/fragment.glsl`) and reflective materials
  (`default/fragment.glsl`) reflect only the skybox cubemap, so objects, terrain and other surfaces never appear in
  reflections. Planar reflections for water (the scene rendered mirrored around the water plane into a texture) and
  optionally a dynamic cubemap for reflective objects would fix it; with the depth of the scene the water also gets
  its color by the depth, a foam line along the shore and refraction.
- [ ] **6. Antialiasing of the post effects.** Multisampling smooths the geometry edges only; with the post effects
  and alpha cutout foliage the image needs FXAA or temporal antialiasing (TAA, which needs motion vectors).

## Animation

- [ ] **7. Animation.** Objects move only from code: there are no animations in the resources. Keyframe
  animation of the transforms of the hierarchy nodes (position, rotation, scale with interpolation) is the first
  step; skeletal animation (skinned meshes with joints and weights, played and blended in the vertex shader) needs a
  format with skeletons, so it comes with a glTF loader, which also brings PBR materials and scenes in one file.

## Editor

- [ ] **8. Editor redesign.** The editor (`editor/`) uses the default light Modena look with a couple of tiny style
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
