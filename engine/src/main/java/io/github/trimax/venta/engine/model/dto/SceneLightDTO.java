package io.github.trimax.venta.engine.model.dto;

import org.joml.Vector3f;

public record SceneLightDTO(String name,
                            Vector3f position,
                            Vector3f direction) {
}
