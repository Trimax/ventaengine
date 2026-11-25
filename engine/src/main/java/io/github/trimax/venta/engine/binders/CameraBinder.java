package io.github.trimax.venta.engine.binders;

import org.joml.Vector3fc;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.CameraInstance;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CameraBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final CameraInstance camera) {
        if (camera == null)
            return;

        bind(program, camera.getPosition());
    }

    /** The position the shaders see the scene from: the one of the camera, or the one of its mirror image in a reflection */
    public void bind(final ProgramEntityImplementation program, final Vector3fc position) {
        bind(program.getUniformID(ShaderUniform.CameraPosition), position);
    }
}
