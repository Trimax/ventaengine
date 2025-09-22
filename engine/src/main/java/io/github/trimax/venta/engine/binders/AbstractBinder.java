package io.github.trimax.venta.engine.binders;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20C.*;

abstract class AbstractBinder {
    protected final void bind(final int uniformID, final boolean value) {
        if (uniformID >= 0)
            glUniform1i(uniformID, value ? 1 : 0);
    }

    protected final void bind(final int uniformID, final int value) {
        if (uniformID >= 0)
            glUniform1i(uniformID, value);
    }

    protected final void bind(final int uniformID, final float value) {
        if (uniformID >= 0)
            glUniform1f(uniformID, value);
    }

    protected final void bind(final int uniformID, final float x, final float y, final float z, final float w) {
        if (uniformID >= 0)
            glUniform4f(uniformID, x, y, z, w);
    }

    protected final void bind(final int uniformID, final float x, final float y, final float z) {
        if (uniformID >= 0)
            glUniform3f(uniformID, x, y, z);
    }

    protected final void bind(final int uniformID, final float x, final float y) {
        if (uniformID >= 0)
            glUniform2f(uniformID, x, y);
    }

    protected final void bind(final int uniformID, final Vector4fc vector) {
        bind(uniformID, vector.x(), vector.y(), vector.z(), vector.w());
    }

    protected final void bind(final int uniformID, final Vector3fc vector) {
        bind(uniformID, vector.x(), vector.y(), vector.z());
    }

    protected final void bind(final int uniformID, final Vector2fc vector) {
        bind(uniformID, vector.x(), vector.y());
    }

    protected final void bind(final int uniformID, final FloatBuffer buffer) {
        if (uniformID >= 0)
            glUniform4fv(uniformID, buffer);
    }

    protected final void bindMatrix3(final int uniformID, final FloatBuffer buffer) {
        if (uniformID >= 0)
            glUniformMatrix3fv(uniformID, false, buffer);
    }

    protected final void bindMatrix4(final int uniformID, final FloatBuffer buffer) {
        if (uniformID >= 0)
            glUniformMatrix4fv(uniformID, false, buffer);
    }
}
