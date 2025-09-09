package io.github.trimax.venta.engine.utils;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector4fc;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class BufferUtil {
    public void write(@NonNull final Matrix4f matrix, @NonNull final FloatBuffer buffer) {
        matrix.get(buffer);
        buffer.position(buffer.position() + 16);
    }

    public void write(@NonNull final Vector4fc vector, @NonNull final FloatBuffer buffer) {
        vector.get(buffer);
        buffer.position(buffer.position() + 4);
    }
}
