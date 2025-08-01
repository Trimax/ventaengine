package io.github.trimax.venta.engine.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.CameraManager;
import io.github.trimax.venta.engine.managers.ConsoleItemManager;
import io.github.trimax.venta.engine.managers.ConsoleManager;
import io.github.trimax.venta.engine.managers.FontManager;
import io.github.trimax.venta.engine.managers.LightManager;
import io.github.trimax.venta.engine.managers.MaterialManager;
import io.github.trimax.venta.engine.managers.MeshManager;
import io.github.trimax.venta.engine.managers.ObjectManager;
import io.github.trimax.venta.engine.managers.ProgramManager;
import io.github.trimax.venta.engine.managers.ResourceManager;
import io.github.trimax.venta.engine.managers.SceneManager;
import io.github.trimax.venta.engine.managers.ShaderManager;
import io.github.trimax.venta.engine.managers.TextureManager;
import io.github.trimax.venta.engine.managers.WindowManager;
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
    private final ResourceManager.ResourceAccessor resourceAccessor;
    private final ResourceManager resourceManager;

    @Getter(AccessLevel.NONE)
    private final MaterialManager.MaterialAccessor materialAccessor;
    private final MaterialManager materialManager;

    @Getter(AccessLevel.NONE)
    private final TextureManager.TextureAccessor textureAccessor;
    private final TextureManager textureManager;

    @Getter(AccessLevel.NONE)
    private final ProgramManager.ProgramAccessor programAccessor;
    private final ProgramManager programManager;

    @Getter(AccessLevel.NONE)
    private final ObjectManager.ObjectAccessor objectAccessor;
    private final ObjectManager objectManager;

    @Getter(AccessLevel.NONE)
    private final ShaderManager.ShaderAccessor shaderAccessor;
    private final ShaderManager shaderManager;

    @Getter(AccessLevel.NONE)
    private final CameraManager.CameraAccessor cameraAccessor;
    private final CameraManager cameraManager;

    @Getter(AccessLevel.NONE)
    private final SceneManager.SceneAccessor sceneAccessor;
    private final SceneManager sceneManager;

    @Getter(AccessLevel.NONE)
    private final LightManager.LightAccessor lightAccessor;
    private final LightManager lightManager;

    @Getter(AccessLevel.NONE)
    private final MeshManager.MeshAccessor meshAccessor;
    private final MeshManager meshManager;

    @Getter(AccessLevel.NONE)
    private final FontManager.FontAccessor fontAccessor;
    private final FontManager fontManager;

    @Getter(AccessLevel.NONE)
    private final ConsoleItemManager.ConsoleItemAccessor consoleItemAccessor;

    @Getter(AccessLevel.NONE)
    private final ConsoleManager.ConsoleAccessor consoleAccessor;

    @Getter(AccessLevel.NONE)
    private final WindowManager.WindowAccessor windowAccessor;

    @Getter(AccessLevel.PACKAGE)
    private final WindowManager windowManager;

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
