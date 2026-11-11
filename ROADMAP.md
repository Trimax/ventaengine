# Roadmap

Known issues found during the code review of 1.2.61 that still have to be fixed.
Paths are relative to `engine/src/main/java/io/github/trimax/venta/engine/` unless stated otherwise.

## Rendering and shaders

- [ ] **Water lighting mixes model and world space.** `shaders/water/vertex.glsl:64,68`, `water/fragment.glsl:164-165,217`:
  `vertexPosition` and the normal are not transformed by `matrixModel`, but are compared with world-space camera and
  light positions. Water at y=1.5 (examples) gets wrong Fresnel, specular and attenuation.
- [ ] **Gerstner wave sum is wrong.** `shaders/water/vertex.glsl:40-61`: `outNormal` is overwritten by each wave (only the
  last wave's normal survives); each wave's phase uses the already displaced position.
- [ ] **Water lighting details.** `water/fragment.glsl`: `computeDirectionalLight` (:175) is never called; point-light
  contributions (:188-192) are not multiplied by `baseColor`.
- [ ] **No upper bound for waves, noises and terrain materials.** `binders/WaveBinder.java:25`, `binders/NoiseBinder.java:21`,
  `binders/MaterialBinder.java:38` vs shader arrays of 16 / 16 / 8. More elements index uniform arrays out of bounds.
  A terrain with 0 materials reads `elevations[-1]` (`terrain/fragment.glsl:165`). Validate counts on load.
- [ ] **`textureHeight` has different types in terrain stages.** `terrain/vertex.glsl:15` (`sampler2D`) vs
  `terrain/fragment.glsl:56` (`sampler2DArray`), both on texture unit 2. This is a link error by the spec and works only
  because the fragment uniform is optimized out. The terrain fragment shader also ignores lights, normal/roughness arrays
  and fog, so what is bound for them is unused.
- [ ] **Billboard frames are not capped.** `renderers/instance/BillboardInstanceRenderer.java:54` vs `shaders/billboard/vertex.glsl`
  `frames[64]`; sprites with more than 64 frames read out of bounds. `sprite.isLooping()` is ignored.
- [ ] **`DebugRenderer` throws NPE without a current scene.** `renderers/common/DebugRenderer.java:43`.
- [ ] **View direction is interpolated per vertex.** `shaders/default/vertex.glsl:35-36`: compute it in the fragment shader;
  normalize the interpolated TBN normal in `getNormal` (`default/fragment.glsl:163`).
- [ ] **Division by zero in shaders.** `waveAmplitude == 0`
  (`water/fragment.glsl:110`), terrain `factor == 0` (`terrain/fragment.glsl:178`), all-zero attenuation in `getAttenuation`.
- [ ] **Sky sampler name mismatch.** `shaders/sky/fragment.glsl:13` declares `skybox`, the binder sets `textureSkybox`
  (`binders/TextureBinder.java:38`). Works only because unit 0 is the default.
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
