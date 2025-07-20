package com.venta.engine.core;

import com.venta.engine.annotations.Component;
import com.venta.engine.managers.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.MethodUtils;

@Slf4j
@Getter
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class Context {
    private final ResourceManager resourceManager;
    private final ProgramManager programManager;
    private final ObjectManager objectManager;
    private final ShaderManager shaderManager;
    private final WindowManager windowManager;
    private final CameraManager cameraManager;
    private final SceneManager sceneManager;

    @SneakyThrows
    void cleanup() {
        cleanup(sceneManager);
        cleanup(objectManager);
        cleanup(programManager);
        cleanup(shaderManager);
        cleanup(cameraManager);
        cleanup(windowManager);
        cleanup(resourceManager);
    }

    @SneakyThrows
    private void cleanup(final AbstractManager<?, ?> manager) {
        MethodUtils.invokeMethod(manager, true, "cleanup");
    }
}
