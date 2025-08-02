package io.github.trimax.venta.engine.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class VentaContext {
    @Getter(AccessLevel.NONE)
    private final ResourceManagerImplementation.ResourceAccessor resourceAccessor;
    private final ResourceManagerImplementation resourceManager;

    @Getter(AccessLevel.NONE)
    private final MaterialManagerImplementation.MaterialAccessor materialAccessor;
    private final MaterialManagerImplementation materialManager;

    @Getter(AccessLevel.NONE)
    private final TextureManagerImplementation.TextureAccessor textureAccessor;
    private final TextureManagerImplementation textureManager;

    @Getter(AccessLevel.NONE)
    private final ProgramManagerImplementation.ProgramAccessor programAccessor;
    private final ProgramManagerImplementation programManager;

    @Getter(AccessLevel.NONE)
    private final ObjectManagerImplementation.ObjectAccessor objectAccessor;
    private final ObjectManagerImplementation objectManager;

    @Getter(AccessLevel.NONE)
    private final ShaderManagerImplementation.ShaderAccessor shaderAccessor;
    private final ShaderManagerImplementation shaderManager;

    @Getter(AccessLevel.NONE)
    private final CameraManagerImplementation.CameraAccessor cameraAccessor;
    private final CameraManagerImplementation cameraManager;

    @Getter(AccessLevel.NONE)
    private final SceneManagerImplementation.SceneAccessor sceneAccessor;
    private final SceneManagerImplementation sceneManager;

    @Getter(AccessLevel.NONE)
    private final LightManagerImplementation.LightAccessor lightAccessor;
    private final LightManagerImplementation lightManager;

    @Getter(AccessLevel.NONE)
    private final MeshManagerImplementation.MeshAccessor meshAccessor;
    private final MeshManagerImplementation meshManager;

    @Getter(AccessLevel.NONE)
    private final FontManagerImplementation.FontAccessor fontAccessor;
    private final FontManagerImplementation fontManager;

    @Getter(AccessLevel.NONE)
    private final ConsoleItemManagerImplementation.ConsoleItemAccessor consoleItemAccessor;

    @Getter(AccessLevel.NONE)
    private final ConsoleManagerImplementation.ConsoleAccessor consoleAccessor;

    @Getter(AccessLevel.NONE)
    private final WindowManagerImplementation.WindowAccessor windowAccessor;

    @Getter(AccessLevel.PACKAGE)
    private final WindowManagerImplementation windowManager;

    @Getter(AccessLevel.PACKAGE)
    private final VentaState state = new VentaState();

    /* The cleanup order is important */
    void cleanup() {
        sceneAccessor.cleanup();
        lightAccessor.cleanup();
        objectAccessor.cleanup();
        meshAccessor.cleanup();
        programAccessor.cleanup();
        shaderAccessor.cleanup();
        cameraAccessor.cleanup();
        windowAccessor.cleanup();
        materialAccessor.cleanup();
        fontAccessor.cleanup();
        consoleAccessor.cleanup();
        consoleItemAccessor.cleanup();
        textureAccessor.cleanup();
        resourceAccessor.cleanup();
    }
}
