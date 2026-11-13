# Roadmap

All the known issues found during the code review of 1.2.61 are fixed. Planned features are listed below.
Paths are relative to `engine/src/main/resources/shaders/` unless stated otherwise.

## Rendering

- [ ] **Environment reflections.** Water (`water/fragment.glsl`) and reflective materials (`default/fragment.glsl`)
  reflect only the skybox cubemap, so objects, terrain and other surfaces never appear in reflections. Planar
  reflections for water (render the scene mirrored around the water plane into a texture) and optionally a dynamic
  cubemap for reflective objects would fix it.
- [ ] **Shadows.** Nothing casts shadows: `castShadows` is read from light definitions and declared in the `Light`
  struct of the shaders, but it is never used. Shadow mapping for the directional light (a depth pass from the light
  and a shadow lookup in the default and terrain shaders) is the first step, point light shadows (cubemap depth maps)
  can follow.
- [ ] **Alpha cutout.** Materials can't cut holes by the texture alpha: `default/fragment.glsl` never discards
  fragments, and a semi-transparent texture is only alpha blended without sorting. Leaves, grass, fences and other
  foliage need an alpha threshold in the material (fragments below it are discarded, the rest are opaque and write
  depth) and double-sided faces for thin geometry like leaf cards.
- [ ] **Shader includes.** GLSL has no `#include` (`GL_ARB_shading_language_include` isn't available on macOS), so
  the same structures and functions (fog, lights and attenuation, `isSet`, noise) are copied into the default,
  terrain, water, particle, billboard and sky shaders and have to be changed in all of them. A small preprocessor in
  `ShaderRegistryImplementation` (between reading the source and `glShaderSource`) could replace
  `#include "common/fog.glsl"` with the file content, include every file once and report cycles, with the shared
  parts moved to `common/`.
