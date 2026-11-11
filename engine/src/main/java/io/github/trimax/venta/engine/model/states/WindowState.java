package io.github.trimax.venta.engine.model.states;

import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.joml.Matrix4f;

@Data
@AllArgsConstructor
public final class WindowState extends AbstractState {
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final VentaEngineInputHandler handler;
    private final long internalID;

    @NonNull
    private final String title;

    private int width;
    private int height;

    /** A minimized window has a zero size; the last valid projection is kept instead of dividing by zero */
    public Matrix4f getProjectionMatrix() {
        if (width > 0 && height > 0)
            projectionMatrix.identity().perspective((float) Math.toRadians(60), (float) width / height, 0.1f, 1000f);

        return projectionMatrix;
    }

    public void handleKeyboardKey(final int key, final int scancode, final int action, final int mods) {
        if (handler != null)
            handler.onKey(key, scancode, action, mods);
    }

    public void handleMouseButton(final int button, final int action, final int mods) {
        if (handler != null)
            handler.onMouseButton(button, action, mods);
    }

    public boolean hasHandler() {
        return handler != null;
    }
}
