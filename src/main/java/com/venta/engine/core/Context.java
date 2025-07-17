package com.venta.engine.core;

import com.venta.engine.annotations.Component;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Component
@AllArgsConstructor
public final class Context {
    private final ResourceManager resourceManager;
    private final ProgramManager programManager;
    private final ObjectManager objectManager;
    private final ShaderManager shaderManager;
    private final WindowManager windowManager;
}
