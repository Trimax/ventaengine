package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final Vector3f color) {
        if (color != null)
            bind(program.getUniformID(ShaderUniform.Color), color);
    }

    public void bindPosition(final ProgramEntityImplementation program, final float x0, final float y0, final float x1, final float y1) {
        bind(program.getUniformID(ShaderUniform.BoundsPosition), x0, y0, x1, y1);
    }

    public void bindTextureCoordinates(final ProgramEntityImplementation program, final float s0, final float t0, final float s1, final float t1) {
        bind(program.getUniformID(ShaderUniform.BoundsTextureCoordinates), s0, t0, s1, t1);
    }
}
