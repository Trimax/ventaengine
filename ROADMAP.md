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

- [ ] **OpenAL buffers are deleted before sources.** `controllers/EngineController.java:42-44` and `memory/Memory.java:33-34`
  clean registries (buffers) before managers (sources); `alDeleteBuffers` fails with `AL_INVALID_OPERATION` and the
  buffers leak silently.
- [ ] **Deleting a scene doesn't release its instances.** `managers/implementation/SceneManagerImplementation.java`: emitters
  and sound sources created for a scene keep their GPU/AL resources until shutdown. Decide on ownership (scene-owned vs
  shared instances) and delete scene-owned instances.
- [ ] **Cubemap load failure leaks and hides the cause.** `registries/implementation/CubemapRegistryImplementation.java:57`
  creates the texture before faces are loaded and never deletes it on failure; `TextureFormat.of` can return null (:115),
  the NPE is swallowed by `loadTextureSafe` (:88-89) without the exception and leaks the image buffer; the format is always
  stored as `RGB` (:78).
- [ ] **Sound loading.** `SoundRegistryImplementation.java:53-54` drops the original exception; the AL buffer and decoded
  data leak if `alBufferData` throws.
- [ ] **Window icon channels.** `utils/IconUtil.java`: `stbi_load_from_memory(..., 0)` keeps the source channel count, but
  GLFW needs RGBA; pass `4`.
- [ ] **GLFW callbacks are never freed.** `controllers/WindowController.java:82-87`, `core/Engine.java:44`.

## Model, math and parsers

- [ ] **Minimized window.** `model/states/WindowState.java:23`: 0x0 size gives a NaN projection matrix;
  `callbacks/FramebufferSizeCallback.java:21-22` updates a matrix that is overwritten on the next `get` (dead code).
- [ ] `CameraInstanceImplementation.rotation` is never updated, so `getRotation()` is always (0, 0, 0).
