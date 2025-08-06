package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.FloatBuffer;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MatrixBinder extends AbstractBinder {
    public void bindViewProjectionMatrix(final ProgramEntityImplementation program, final FloatBuffer matrixViewProjection) {
        bindMatrix4(program.getUniformID(ShaderUniform.MatrixViewProjection), matrixViewProjection);
    }

    public void bindNormalMatrix(final ProgramEntityImplementation program, final FloatBuffer matrixNormal) {
        bindMatrix3(program.getUniformID(ShaderUniform.MatrixNormal), matrixNormal);
    }

    public void bindModelMatrix(final ProgramEntityImplementation program, final FloatBuffer matrixModel) {
        bindMatrix4(program.getUniformID(ShaderUniform.MatrixModel), matrixModel);
    }
}
