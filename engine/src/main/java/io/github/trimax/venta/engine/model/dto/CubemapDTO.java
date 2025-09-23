package io.github.trimax.venta.engine.model.dto;

import java.util.Map;

import io.github.trimax.venta.engine.enums.CubemapFace;
import lombok.NonNull;

public record CubemapDTO(@NonNull String program,
                         @NonNull Map<CubemapFace, String> faces) {
    public String getTexturePath(@NonNull final CubemapFace face) {
        return faces.getOrDefault(face, null);
    }
}
