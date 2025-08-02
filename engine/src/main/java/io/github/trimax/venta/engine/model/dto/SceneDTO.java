package io.github.trimax.venta.engine.model.dto;

import org.apache.commons.collections4.CollectionUtils;
import org.joml.Vector4f;

import java.util.List;

public record SceneDTO(List<SceneObjectDTO> objects,
                       List<SceneLightDTO> lights,
                       Vector4f ambientLight) {
    public boolean hasObjects() {
        return CollectionUtils.isNotEmpty(objects);
    }

    public boolean hasLights() {
        return CollectionUtils.isNotEmpty(lights);
    }
}