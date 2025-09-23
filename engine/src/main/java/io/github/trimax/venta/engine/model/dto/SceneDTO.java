package io.github.trimax.venta.engine.model.dto;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.scene.SceneBillboardDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneEmitterDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneFogDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneLightDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneObjectDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneSoundSourceDTO;

public record SceneDTO(List<SceneLightDTO> lights,
                       List<SceneObjectDTO> objects,
                       List<SceneEmitterDTO> emitters,
                       List<SceneBillboardDTO> billboards,
                       List<SceneSoundSourceDTO> soundSources,
                       Vector3f ambientLight,
                       SceneFogDTO fog,
                       String skybox) {
    public boolean hasLights() {
        return CollectionUtils.isNotEmpty(lights);
    }

    public boolean hasObjects() {
        return CollectionUtils.isNotEmpty(objects);
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