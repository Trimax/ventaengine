package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.model.dto.EmitterDTO;
import io.github.trimax.venta.engine.model.prefabs.EmitterPrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class EmitterPrefabImplementation extends AbstractPrefabImplementation implements EmitterPrefab {
    @NonNull
    EmitterDTO dto; //TODO: Remove DTO from here
}
