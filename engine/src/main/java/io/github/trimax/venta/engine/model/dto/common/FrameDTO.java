package io.github.trimax.venta.engine.model.dto.common;

public record FrameDTO(float x, float y, float width, float height) {
    public FrameDTO normalize(final int textureWidth, final int textureHeight) {
        return new FrameDTO(x() / textureWidth, y() / textureHeight, width() / textureWidth, height() / textureHeight);
    }
}
