package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.instance.ProgramInstance;
import io.github.trimax.venta.engine.model.view.CameraView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CameraBinder extends AbstractBinder {
    public void bind(final ProgramInstance program, final CameraView camera) {
        if (camera == null)
            return;

        bind(program.getUniformID(ShaderUniform.CameraPosition), camera.getPosition());
    }
}
