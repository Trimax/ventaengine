package io.github.trimax.venta.engine.model.dto;

import java.util.List;

import io.github.trimax.venta.engine.model.dto.common.ColorDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneBillboardDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneDirectionalLightDTO;
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
                       SceneDirectionalLightDTO directionalLight,
                       ColorDTO ambientLight,
                       SceneFogDTO fog,
                       String skybox) {
}