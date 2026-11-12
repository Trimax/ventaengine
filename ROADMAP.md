# Roadmap

Known issues found during the code review of 1.2.61 that still have to be fixed.
Paths are relative to `engine/src/main/java/io/github/trimax/venta/engine/` unless stated otherwise.

## Rendering and shaders

- [ ] **Terrain shading is incomplete.** `terrain/fragment.glsl` ignores lights, the normal and roughness texture arrays
  and fog, so what `TerrainSurfaceInstanceRenderer` binds for them is unused.
