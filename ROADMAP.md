# Roadmap

Known issues found during the code review of 1.2.61 that still have to be fixed.
Paths are relative to `engine/src/main/java/io/github/trimax/venta/engine/` unless stated otherwise.

## Rendering and shaders

- [ ] **Terrain shading is incomplete.** `terrain/fragment.glsl` ignores lights, the normal and roughness texture arrays
  and fog, so what `TerrainSurfaceInstanceRenderer` binds for them is unused.
- [ ] **View direction is interpolated per vertex.** `shaders/default/vertex.glsl:35-36`: compute it in the fragment shader;
  normalize the interpolated TBN normal in `getNormal` (`default/fragment.glsl:163`).
- [ ] **Division by zero in shaders.** Terrain `factor == 0` (`terrain/fragment.glsl:178`), all-zero attenuation in `getAttenuation`.
- [ ] Minor: `useTextureDebug` is always 1 (`ObjectInstanceRenderer:52`, `TerrainSurfaceInstanceRenderer:48`);
  `shaders/debug/vertex.glsl:11` ignores `matrixModel`; light uniforms are re-uploaded for every object every frame.

## Resources and lifecycle

- [ ] **Deleting a scene doesn't release its instances.** `managers/implementation/SceneManagerImplementation.java`: emitters
  and sound sources created for a scene keep their GPU/AL resources until shutdown. Decide on ownership (scene-owned vs
  shared instances) and delete scene-owned instances.
