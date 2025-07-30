package com.venta.engine.model.dto;

import org.joml.Vector3f;

public record ObjectDTO(Vector3f position,
                        Vector3f angles,
                        Vector3f scale,
                        String program,
                        String mesh) {
}