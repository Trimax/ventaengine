package com.venta.engine.binders;

import com.venta.engine.annotations.Component;
import com.venta.engine.enums.ShaderUniform;
import com.venta.engine.managers.ProgramManager;
import com.venta.engine.model.view.CameraView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CameraBinder extends AbstractBinder {
    public void bind(final ProgramManager.ProgramEntity program, final CameraView camera) {
        if (camera == null)
            return;

        bind(program.getUniformID(ShaderUniform.CameraPosition), camera.getPosition());
    }
}
