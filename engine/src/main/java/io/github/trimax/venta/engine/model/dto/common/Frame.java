package io.github.trimax.venta.engine.model.dto.common;

public record Frame(float x, float y, float width, float height) {
    public Frame normalize(final int textureWidth, final int textureHeight) {
        return new Frame(x() / textureWidth, y() / textureHeight, width() / textureWidth, height() / textureHeight);
    }
}
