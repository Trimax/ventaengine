package com.venta.engine.binders;

import com.venta.container.annotations.Component;
import com.venta.engine.enums.ShaderUniform;
import com.venta.engine.managers.ProgramManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.FloatBuffer;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MatrixBinder extends AbstractBinder {
    public void bind(final ProgramManager.ProgramEntity program, final FloatBuffer matrixViewProjection, final FloatBuffer matrixModel, final FloatBuffer matrixNormal) {
        bindMatrix4(program.getUniformID(ShaderUniform.MatrixViewProjection), matrixViewProjection);
        bindMatrix4(program.getUniformID(ShaderUniform.MatrixModel), matrixModel);
        bindMatrix3(program.getUniformID(ShaderUniform.MatrixNormal), matrixNormal);
    }
}
