package io.github.trimax.venta.engine.model.common.water;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.water.WaterMaterialDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class WaterMaterial {
    @NonNull
    Vector3f colorSurface;

    @NonNull
    Vector3f colorDepth;

    @NonNull
    Vector3f colorPeak;

    float metalness;
    float opacity;

    public WaterMaterial(@NonNull final WaterMaterialDTO dto) {
        this(dto.colorSurface().toVector3f(), dto.colorDepth().toVector3f(), dto.colorPeak().toVector3f(),
                dto.metalness(), dto.opacity());
    }

    public WaterMaterial(@NonNull final WaterMaterial material) {
        this(new Vector3f(material.colorSurface), new Vector3f(material.colorDepth), new Vector3f(material.colorPeak),
                material.getMetalness(), material.getOpacity());
    }
}
