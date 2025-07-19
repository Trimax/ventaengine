package com.venta.engine.model.memory;

import com.venta.engine.model.math.Vector2;
import com.venta.engine.model.math.Vector3;

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
