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
not drawn at all. An object can have simpler models that replace its main model when it is small on the screen (`"lods"`, cross fading by a dither, the last level can be a card with a picture); the levels of terrain are not made yet. Fog is a layer or exponential (it thins out with the height), glows towards the sun and hazes the horizon. The corners and the contacts are darker (`"ambientOcclusion"` of the scene, from the depth). The sun shines through the fog in light shafts where nothing shadows it. Bright parts of the image glow (`"bloom"` of the scene) and the sun can be a disc in the sky that glows with it (`"discSize"` of the directional light). Point lights fade to nothing at their range. A material that follows the wind (`"windInfluence"`, the wind of the scene is `"wind"`) sways with it.
Paths are relative to `engine/src/main/resources/shaders/` unless stated otherwise.

## Rendering

- [ ] **1. Antialiasing of the post effects.** Multisampling smooths the geometry edges only; the image with the bloom, the
  ambient occlusion, the light shafts and the alpha cutout foliage has edges that it doesn't touch. FXAA (a pass over the tone
  mapped image that smooths the edges by the contrast of the brightness) is cheap and needs nothing from the scene. Temporal
  antialiasing is a task of its own, after the animation.
- [ ] **2. Spot lights.** There is one kind of light, the point one (the `type` in a light file is not read). A spot light
  is a cone: a position, a direction, an inner and an outer angle with a falloff between them, lit by the same loops of the
  shaders. Its shadow is one face of the machinery of the point lights: a plain shadow map, drawn with the perspective of the
  cone.
- [ ] **3. Terrain materials.** Terrain materials are picked by the height only, so rock never appears on steep
  slopes and textures stretch on cliffs. Materials should be blended by the height and the slope, sampled
  triplanarly on steep parts, with a detail normal map close to the camera.
- [ ] **4. Water and environment reflections.** Water (`water/fragment.glsl`) and reflective materials
  (`default/fragment.glsl`) reflect only the skybox cubemap, so objects, terrain and other surfaces never appear in
  reflections. Planar reflections for water (the scene rendered mirrored around the water plane into a texture) and
  optionally a dynamic cubemap for reflective objects would fix it; with the depth of the scene the water also gets
  its color by the depth, a foam line along the shore and refraction.
- [ ] **5. Terrain chunks and levels of detail.** A terrain is one grid that is drawn whole with one call, so far away
  it costs as much as close and every vertex of it is displaced by the heightmap. It should be cut into chunks that
  are culled by the view and drawn with coarser grid cells with the distance (the borders of chunks of different
  levels must not crack, for example with skirts or by morphing the vertices on the border).

## Animation

- [ ] **6. Animation.** Objects move only from code: there are no animations in the resources. Keyframe
  animation of the transforms of the hierarchy nodes (position, rotation, scale with interpolation) is the first
  step, and the boxes of the nodes must follow them, or the culling would cut a moving object; skeletal animation
  (skinned meshes with joints and weights, played and blended in the vertex shader) needs a format with skeletons, so
  it comes with a glTF loader, which also brings PBR materials and scenes in one file.

## Later

- [ ] **7. Temporal antialiasing.** The image with the post effects shimmers on thin things (leaves, wires) in motion,
  which FXAA only softens. TAA keeps the history of the frames and needs the motion vectors of what moves: the camera,
  the objects and the skinned meshes, so it comes after the animation.

- [ ] **8. Volumetric clouds.** The clouds are painted on the 8 bit skybox: they don't move, they cast no shadows on the
  land and no light shafts, and the sun can't be hidden by one. Clouds made of a noise (a march through a layer of
  the sky, or a few layers of cards), lit by the sun and the sky, would drift, shadow the land through the shadow maps
  and the light shafts, and change the light of the scene as they pass.
- [ ] **9. Ambient occlusion of the indirect light only.** The ambient occlusion multiplies the finished image, so it
  darkens the direct light of the sun in a corner as well, where the shadow maps decide what is lit. The shaders of
  the surfaces should read it and apply it to the light of the sky and to the image based lighting only. For that
  the occlusion must be known before the surfaces are drawn: a depth pass over the objects and the terrain (with the
  wind, the holes of the cutouts and the instancing), the occlusion from its depth, and then the pass of the colors with
  the depth test that accepts what the depth pass wrote (the vertices must give the same depths in both).

## Editor

- [ ] **10. Editor redesign.** The editor (`editor/`) uses the default light Modena look with a couple of tiny style
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
