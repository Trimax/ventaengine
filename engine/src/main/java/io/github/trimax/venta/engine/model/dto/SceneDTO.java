package io.github.trimax.venta.engine.model.dto;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.common.scene.Fog;
import io.github.trimax.venta.engine.model.dto.scene.SceneBillboardDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneEmitterDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneLightDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneObjectDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneSoundSourceDTO;

public record SceneDTO(List<SceneObjectDTO> objects,
                       List<SceneLightDTO> lights,
                       List<SceneEmitterDTO> emitters,
                       List<SceneBillboardDTO> billboards,
                       List<SceneSoundSourceDTO> soundSources,
                       String skybox,
                       Fog fog,
                       Vector3f ambientLight) {
    public boolean hasObjects() {
        return CollectionUtils.isNotEmpty(objects);
    }

    public boolean hasLights() {
        return CollectionUtils.isNotEmpty(lights);
    }

    public boolean hasEmitters() {
        return CollectionUtils.isNotEmpty(emitters);
    }

    public boolean hasBillboards() {
        return CollectionUtils.isNotEmpty(billboards);
    }

    public boolean hasSoundSources() {
        return CollectionUtils.isNotEmpty(soundSources);
    }
}