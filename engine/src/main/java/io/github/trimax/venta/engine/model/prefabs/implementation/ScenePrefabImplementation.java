package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.engine.model.prefabs.ScenePrefab;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ScenePrefabImplementation extends AbstractPrefabImplementation implements ScenePrefab {
    SceneDTO dto;
}