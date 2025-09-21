package io.github.trimax.venta.engine.model.common.dto;

public record Frame(float x, float y, float width, float height) {
    public Frame normalize(final int textureWidth, final int textureHeight) {
        return new Frame(x() / textureWidth, y() / textureHeight, width() / textureWidth, height() / textureHeight);
    }
}
