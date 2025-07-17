package com.venta.engine.model;

public record Vertex(Vector3 position, Vector3 normal, Vector2 textureCoordinates, Color color) {
    public boolean hasPosition() {
        return position != null;
    }

    public boolean hasNormal() {
        return normal != null;
    }

    public boolean hasTextureCoordinates() {
        return textureCoordinates != null;
    }

    public boolean hasColor() {
        return color != null;
    }
}
