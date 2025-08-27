package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.engine.model.common.scene.Fog;
import org.apache.commons.collections4.CollectionUtils;
import org.joml.Vector3f;

import java.util.List;

public record SceneDTO(List<SceneObjectDTO> objects,
                       List<SceneLightDTO> lights,
                       String skybox,
                       Fog fog,
                       Vector3f ambientLight) {
    public boolean hasObjects() {
        return CollectionUtils.isNotEmpty(objects);
    }

    public boolean hasLights() {
        return CollectionUtils.isNotEmpty(lights);
    }
}