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
- [ ] **Gizmos are never removed.** `GizmoManagerImplementation` gets a gizmo for every object/light/emitter/sound/camera,
  but no `destroy()` removes it.
- [ ] **Registry cache ignores the argument.** `registries/implementation/AbstractRegistryImplementation.java:22-28` keys
  only by path: `ShaderRegistry.get(path, Vertex)` then `get(path, Fragment)` returns the vertex shader; failed shaders are
  not deleted (`ShaderRegistryImplementation.java:37-38`).
- [ ] **Sound loading.** `SoundRegistryImplementation.java:53-54` drops the original exception; the AL buffer and decoded
  data leak if `alBufferData` throws.
- [ ] **Window icon channels.** `utils/IconUtil.java`: `stbi_load_from_memory(..., 0)` keeps the source channel count, but
  GLFW needs RGBA; pass `4`.
- [ ] **GLFW callbacks are never freed.** `controllers/WindowController.java:82-87`, `core/Engine.java:44`.

## Model, math and parsers

- [ ] **Minimized window.** `model/states/WindowState.java:23`: 0x0 size gives a NaN projection matrix;
  `callbacks/FramebufferSizeCallback.java:21-22` updates a matrix that is overwritten on the next `get` (dead code).
- [ ] `CameraInstanceImplementation.rotation` is never updated, so `getRotation()` is always (0, 0, 0).

## Console, input and container

- [ ] **Container static state is never reset.** `container/.../VentaApplication.java:20-22,58-69,116-119`:
  `knownComponents` is never cleared and `creationStack` is not cleaned on exceptions, so container tests depend on the
  execution order and cycle detection can pass for the wrong reason.
- [ ] **Injection order is random.** `container/.../VentaApplication.java:110-113` iterates a `HashSet`; `help` output and
  injected lists have a JVM-dependent order.
- [ ] `executors/scene/SceneCreateExecutor.java:25` creates the scene from the untrimmed `command.value()`.
- [ ] `model/states/ConsoleState.java:48-49` checks `history.isEmpty()` instead of `commands.isEmpty()` for up-arrow history.
- [ ] `executors/camera/CameraDescribeExecutor.java:38` prints `Right: %b` for a vector (always `true`).

## Editor (`editor/src/main/java/io/github/trimax/venta/editor/`)

- [ ] **Some resource types can't be put into an archive.** `engine/.../enums/GroupType.java` has no groups for
  sound files (`/sounds/`), fonts (`/fonts/`), terrain and water surfaces (`/surfaces/terrain/`, `/surfaces/water/`)
  and texture arrays (`/texturearrays/`), so the editor can't pack them.
- [ ] **Duplicate names break archive loading.** `ResourceAddListener.java:26` has no duplicate check (and the check in
  `TreeUtil.isItemExist` is case-sensitive); `ResourceService.loadArchive` `toMap` throws on duplicate keys.
- [ ] **Absolute paths in the project JSON.** `ResourceAddListener.java:26` stores `getAbsolutePath()`; on another machine
  `ArchiveBuildListener.java:57` silently writes empty nodes and still reports success.
- [ ] **Silent failures.** `EventUtil` uses a plain `EventBus` that only logs subscriber exceptions; listeners use
  `@SneakyThrows`, so failures of save, load and build are not shown to the user. Show errors in the UI.
- [ ] **NPE for Emitters / Grid meshes.** `ItemRenderer.java:20-33` has no entries for them; `PropertiesController.java:57`.
- [ ] **Listener leak.** `TreeUtil.initialize` (`TreeUtil.java:22-23`) adds a selection listener on every "New archive";
  auto-sort is attached only to the initial root.
- [ ] `MixerController.java:28` passes a null image to `ImageUtil.write` when not all channels are set;
  `ImageUtil.java:69-77` throws from inside a native STB callback.
