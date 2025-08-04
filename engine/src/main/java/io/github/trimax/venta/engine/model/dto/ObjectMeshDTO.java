package io.github.trimax.venta.engine.model.dto;

import lombok.NonNull;
import org.joml.Vector3f;

import java.util.List;

public record ObjectMeshDTO(@NonNull String name,
                            String material,
                            Vector3f position,
                            Vector3f angles,
                            Vector3f scale,
                            List<ObjectMeshDTO> children) {
}
