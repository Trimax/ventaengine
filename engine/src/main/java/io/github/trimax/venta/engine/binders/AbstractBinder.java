package io.github.trimax.venta.engine.binders;

import static org.lwjgl.opengl.GL20C.*;

import java.nio.FloatBuffer;

import org.joml.Vector3f;
import org.joml.Vector4f;

abstract class AbstractBinder {
    protected final void bind(final int uniformID, final int value) {
        if (uniformID >= 0)
            glUniform1i(uniformID, value);
    }

    protected final void bind(final int uniformID, final float value) {
        if (uniformID >= 0)
            glUniform1f(uniformID, value);
    }

    protected final void bind(final int uniformID, final Vector4f vector) {
        if (uniformID >= 0)
            glUniform4f(uniformID, vector.x, vector.y, vector.z, vector.w);
    }

    protected final void bind(final int uniformID, final Vector3f vector) {
        if (uniformID >= 0)
            glUniform3f(uniformID, vector.x, vector.y, vector.z);
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
