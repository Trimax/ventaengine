package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.model.dto.LightPrefabDTO;
import io.github.trimax.venta.engine.model.prefabs.LightPrefab;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor
public class LightPrefabImplementation extends AbstractPrefabImplementation implements LightPrefab {
    @NonNull
    LightPrefabDTO dto;
}