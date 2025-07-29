package com.venta.engine.core;

import com.venta.container.annotations.Component;
import com.venta.engine.interfaces.VentaEngineConfiguration;
import com.venta.engine.managers.CameraManager;
import com.venta.engine.managers.LightManager;
import com.venta.engine.managers.MaterialManager;
import com.venta.engine.managers.MeshManager;
import com.venta.engine.managers.ObjectManager;
import com.venta.engine.managers.ProgramManager;
import com.venta.engine.managers.ResourceManager;
import com.venta.engine.managers.SceneManager;
import com.venta.engine.managers.ShaderManager;
import com.venta.engine.managers.TextureManager;
import com.venta.engine.managers.WindowManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class VentaContext {
    @Setter(value = AccessLevel.PACKAGE, onParam_ = @__(@NonNull))
    private VentaEngineConfiguration.RenderConfiguration renderConfiguration;

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
    private final MeshManager.MeshAccessor meshAccessor;
    private final MeshManager meshManager;

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
    private final WindowManager.WindowAccessor windowAccessor;

    @Getter(AccessLevel.PACKAGE)
    private final WindowManager windowManager;

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
        textureAccessor.cleanup();
        resourceAccessor.cleanup();
    }
}
