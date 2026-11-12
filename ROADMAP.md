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
