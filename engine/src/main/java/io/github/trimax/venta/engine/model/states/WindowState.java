package io.github.trimax.venta.engine.model.states;

import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import io.github.trimax.venta.engine.model.common.shared.View;
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

    /** Multisampling of the scene framebuffer (the window itself has none, it only shows the tone mapped image) */
    private final int samples;

    /** How the camera sees the scene shown: the engine sets it from the scene in every frame */
    private final View view = new View();

    /** A minimized window has a zero size; the last valid projection is kept instead of dividing by zero */
    public Matrix4f getProjectionMatrix() {
        if (width > 0 && height > 0)
            projectionMatrix.identity().perspective((float) Math.toRadians(view.getFov()), (float) width / height, view.getNear(), view.getFar());

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
