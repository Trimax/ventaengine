package com.venta.engine.utils;

import java.nio.FloatBuffer;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class MatrixUtil {
    private static final FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer mat3Buffer = BufferUtils.createFloatBuffer(9);

    public Matrix4f createModelMatrix(final Vector3f translation, final Vector3f rotationEuler, final Vector3f scale) {
        return new Matrix4f()
                .identity()
                .translate(translation)
                .rotateX(rotationEuler.x)
                .rotateY(rotationEuler.y)
                .rotateZ(rotationEuler.z)
                .scale(scale);
    }

    public Matrix3f createNormalMatrix(final Matrix4f modelMatrix) {
        final var normalMatrix = new Matrix3f();
        modelMatrix.normal(normalMatrix);

        return normalMatrix;
    }

    public static Matrix4f createViewProjectionMatrix(final Matrix4f projection, final Matrix4f view) {
        return new Matrix4f(projection).mul(view);
    }

    // Отправка матрицы 4x4 в шейдер
//    public static void setUniformMat4(int programId, String uniformName, Matrix4f matrix) {
//        int location = glGetUniformLocation(programId, uniformName);
//        if (location >= 0) {
//            matrix.get(mat4Buffer);
//            mat4Buffer.flip();
//            glUniformMatrix4fv(location, false, mat4Buffer);
//        }
//    }
//
//    // Отправка матрицы 3x3 в шейдер
//    public static void setUniformMat3(int programId, String uniformName, Matrix3f matrix) {
//        int location = glGetUniformLocation(programId, uniformName);
//        if (location >= 0) {
//            matrix.get(mat3Buffer);
//            mat3Buffer.flip();
//            glUniformMatrix3fv(location, false, mat3Buffer);
//        }
//    }
}
