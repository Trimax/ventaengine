package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.light.Attenuation;
import io.github.trimax.venta.engine.model.dto.LightPrefabDTO;
import io.github.trimax.venta.engine.model.prefabs.ScenePrefab;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.joml.Vector3f;

@Value
@AllArgsConstructor
public class ScenePrefabImplementation extends AbstractPrefabImplementation implements ScenePrefab {
    LightType type;
    Vector3f color;
    Attenuation attenuation;
    float intensity;
    float range;
    boolean castShadows;

    public ScenePrefabImplementation(final LightPrefabDTO dto) {
        this(dto.type(), dto.color(), dto.attenuation().getAttenuation(), dto.intensity(), dto.range(), dto.castShadows());
    }
}