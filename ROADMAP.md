# Roadmap

All the known issues found during the code review of 1.2.61 are fixed. The planned features are listed below in the
order they will be done: every step builds on the previous ones and makes a visible difference on its own.
Paths are relative to `engine/src/main/resources/shaders/` unless stated otherwise.

## Rendering

- [ ] **1. HDR, tone mapping and sRGB.** The scene is rendered straight to the window in 8 bits per channel without
  gamma correction: bright lights clip to white, lighting is computed on sRGB colors and the image looks flat. The
  scene should be rendered into a floating point framebuffer (with the multisampling of the window), then a full
  screen pass applies the exposure, a filmic tone mapping (ACES) and the sRGB encoding; textures with colors are read
  as sRGB, so lighting is linear. The console, the text and the debug overlays are drawn after the tone mapping.
  Every following step (bloom, SSAO, post effects) renders into this framebuffer.
- [ ] **2. Shader includes.** GLSL has no `#include` (`GL_ARB_shading_language_include` isn't available on macOS), so
  the same structures and functions (fog, lights and attenuation, `isSet`, noise) are copied into the default,
  terrain, water, particle, billboard and sky shaders and have to be changed in all of them. A small preprocessor in
  `ShaderRegistryImplementation` (between reading the source and `glShaderSource`) could replace
  `#include "common/fog.glsl"` with the file content, include every file once and report cycles, with the shared
  parts moved to `common/`. It goes before the lighting changes, which touch every shader.
- [ ] **3. Shadows.** Nothing casts shadows: `castShadows` is read from light definitions and declared in the `Light`
  struct of the shaders, but it is never used. Cascaded shadow maps for the directional light (depth passes from the
  sun for a few distance ranges and a filtered shadow lookup in the default, terrain and water shaders) are the first
  step, point light shadows (cubemap depth maps) can follow.
- [ ] **4. Physically based lighting.** The shaders use Blinn-Phong like lighting, so materials look like plastic.
  A Cook-Torrance BRDF (GGX distribution, Smith geometry, Schlick fresnel) driven by the metalness and roughness of
  the materials, and image based lighting from the skybox (a diffuse irradiance cubemap and a specular cubemap
  prefiltered by roughness with a BRDF lookup table) give the objects the color of the sky around them.
- [ ] **5. Alpha cutout.** Materials can't cut holes by the texture alpha: `default/fragment.glsl` never discards
  fragments, and a semi-transparent texture is only alpha blended without sorting. Leaves, grass, fences and other
  foliage need an alpha threshold in the material (fragments below it are discarded, the rest are opaque and write
  depth) and double-sided faces for thin geometry like leaf cards.
- [ ] **6. Instancing and foliage.** Every object is a separate draw call, so a few hundred trees already cost a lot.
  Objects sharing a mesh and a material should be drawn with instanced rendering (a buffer of model matrices), which
  makes forests and grass possible; foliage also needs a wind sway in the vertex shader.
- [ ] **7. Levels of detail.** A far object costs as much as a close one. Objects should have several meshes
  (simpler with the distance, the last one can be a billboard with a picture of the object) and switch between them
  by the distance or the screen size, with a short crossfade (dithered) so the switch doesn't pop; terrain far away
  can use coarser grid cells as well.
- [ ] **8. Ambient occlusion.** Contact shadows in corners, under walls and around the roots of trees are missing, so
  objects look like floating on the ground. Screen space ambient occlusion (from the depth and the normals of the
  scene, blurred) darkens the ambient and the image based lighting there.
- [ ] **9. Atmosphere.** The fog is a layer with a sharp top, and bright light has no glow. An exponential height fog
  (the density decreases with the height, integrated along the view ray) with the sun color scattered towards the
  sun, light shafts (god rays) and bloom (bright parts of the HDR image blurred and added back) give the hazy,
  glowing look of sunsets and forests.
- [ ] **10. Terrain materials.** Terrain materials are picked by the height only, so rock never appears on steep
  slopes and textures stretch on cliffs. Materials should be blended by the height and the slope, sampled
  triplanarly on steep parts, with a detail normal map close to the camera.
- [ ] **11. Water and environment reflections.** Water (`water/fragment.glsl`) and reflective materials
  (`default/fragment.glsl`) reflect only the skybox cubemap, so objects, terrain and other surfaces never appear in
  reflections. Planar reflections for water (the scene rendered mirrored around the water plane into a texture) and
  optionally a dynamic cubemap for reflective objects would fix it; with the depth of the scene the water also gets
  its color by the depth, a foam line along the shore and refraction.
- [ ] **12. Antialiasing of the post effects.** Multisampling smooths the geometry edges only; with the post effects
  and alpha cutout foliage the image needs FXAA or temporal antialiasing (TAA, which needs motion vectors).

## Animation

- [ ] **13. Animation.** Objects move only from code: there are no animations in the resources. Keyframe
  animation of the transforms of the hierarchy nodes (position, rotation, scale with interpolation) is the first
  step; skeletal animation (skinned meshes with joints and weights, played and blended in the vertex shader) needs a
  format with skeletons, so it comes with a glTF loader, which also brings PBR materials and scenes in one file.
