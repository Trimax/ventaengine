package com.venta.engine.core;

import org.apache.commons.lang3.reflect.MethodUtils;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.AbstractManager;
import com.venta.engine.managers.CameraManager;
import com.venta.engine.managers.MaterialManager;
import com.venta.engine.managers.ObjectManager;
import com.venta.engine.managers.ProgramManager;
import com.venta.engine.managers.ResourceManager;
import com.venta.engine.managers.SceneManager;
import com.venta.engine.managers.ShaderManager;
import com.venta.engine.managers.TextureManager;
import com.venta.engine.managers.WindowManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class Context {
    private final ResourceManager resourceManager;
    private final MaterialManager materialManager;
    private final TextureManager textureManager;
    private final ProgramManager programManager;
    private final ObjectManager objectManager;
    private final ShaderManager shaderManager;
    private final CameraManager cameraManager;
    private final SceneManager sceneManager;

    @Getter(AccessLevel.PACKAGE)
    private final WindowManager windowManager;

    /* The cleanup order is important */
    @SneakyThrows
    void cleanup() {
        cleanup(sceneManager);
        cleanup(objectManager);
        cleanup(programManager);
        cleanup(shaderManager);
        cleanup(cameraManager);
        cleanup(windowManager);
        cleanup(materialManager);
        cleanup(textureManager);
        cleanup(resourceManager);
    }

    @SneakyThrows
    private void cleanup(final AbstractManager<?, ?> manager) {
        MethodUtils.invokeMethod(manager, true, "cleanup");
    }
}
