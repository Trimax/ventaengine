package com.venta.engine.core;

import com.venta.engine.annotations.Component;
import com.venta.engine.manager.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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

    void cleanup() {
        //programManager.destroy();
        //shaderManager.destroy();
        //objectManager.destroy();
        //windowManager.destroy();
        //resourceManager.destroy();
    }
}
