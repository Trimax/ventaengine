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

    public Matrix4f getProjectionMatrix() {
        final float aspectRatio = (float) width / height;
        return projectionMatrix.identity().perspective((float) Math.toRadians(60), aspectRatio, 0.1f, 1000f);
    }

    public boolean hasHandler() {
        return handler != null;
    }
}
