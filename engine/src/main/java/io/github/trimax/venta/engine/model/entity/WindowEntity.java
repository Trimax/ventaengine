package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joml.Matrix4f;

//TODO: Should be state hierarchy
@Slf4j
@Getter
public final class WindowEntity extends AbstractEntity {
    private final VentaEngineInputHandler handler;
    private final Matrix4f projectionMatrix;
    private final long internalID;

    @Setter
    private int width;

    @Setter
    private int height;

    public WindowEntity(final long internalID,
                        final int width,
                        final int height,
                        @NonNull final String title,
                        @NonNull final VentaEngineInputHandler inputHandler) {
        super(title);

        this.handler = inputHandler;

        this.internalID = internalID;
        this.width = width;
        this.height = height;

        final float aspectRatio = (float) width / height;
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(60), aspectRatio, 0.1f, 1000f);
    }

    public boolean hasHandler() {
        return handler != null;
    }
}
