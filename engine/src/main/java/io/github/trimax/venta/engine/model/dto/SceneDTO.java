package io.github.trimax.venta.engine.model.dto;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.joml.Vector3f;

public record SceneDTO(List<SceneObjectDTO> objects,
                       List<SceneLightDTO> lights,
                       Vector3f ambientLight) {
    public boolean hasObjects() {
        return CollectionUtils.isNotEmpty(objects);
    }

    public boolean hasLights() {
        return CollectionUtils.isNotEmpty(lights);
    }
}