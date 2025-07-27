package com.venta.engine.core;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class Context {
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
    private final NewObjectManager.NewObjectAccessor newObjectAccessor;
    private final NewObjectManager newObjectManager;

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
    private final WindowManager.WindowAccessor windowAccessor;

    @Getter(AccessLevel.PACKAGE)
    private final WindowManager windowManager;

    /* The cleanup order is important */
    void cleanup() {
        sceneAccessor.cleanup();
        lightAccessor.cleanup();
        newObjectAccessor.cleanup();
        objectAccessor.cleanup();
        programAccessor.cleanup();
        shaderAccessor.cleanup();
        cameraAccessor.cleanup();
        windowAccessor.cleanup();
        materialAccessor.cleanup();
        textureAccessor.cleanup();
        resourceAccessor.cleanup();
    }
}
