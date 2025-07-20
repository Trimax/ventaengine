package com.venta.engine.model.memory;

import org.joml.Vector2i;
import org.joml.Vector3f;

public record Vertex(Vector3f position, Vector3f normal, Vector2i textureCoordinates, Color color) {
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
