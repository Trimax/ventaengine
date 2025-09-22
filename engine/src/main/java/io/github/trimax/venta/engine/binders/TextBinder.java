package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextBinder extends AbstractBinder {
    private static final Vector4f BUFFER = new Vector4f();

    public void bind(final ProgramEntityImplementation program, final Vector3f color) {
        if (color != null)
            bind(program.getUniformID(ShaderUniform.Color), color);
    }

    public void bind1(final ProgramEntityImplementation program, final float x0, final float y0, final float x1, final float y1) {
        BUFFER.set(x0, y0, x1, y1);

        bind(program.getUniformID(ShaderUniform.BoundsPosition), BUFFER);
    }

    public void bind2(final ProgramEntityImplementation program, final float x0, final float y0, final float x1, final float y1) {
        BUFFER.set(x0, y0, x1, y1);

        bind(program.getUniformID(ShaderUniform.BoundsTextureCoordinates), BUFFER);
    }
}
