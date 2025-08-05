package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.instance.CameraInstance;
import io.github.trimax.venta.engine.model.instance.implementation.ProgramInstanceImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CameraBinder extends AbstractBinder {
    public void bind(final ProgramInstanceImplementation program, final CameraInstance camera) {
        if (camera == null)
            return;

        bind(program.getUniformID(ShaderUniform.CameraPosition), camera.getPosition());
    }
}
