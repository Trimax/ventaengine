package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.light.Attenuation;
import io.github.trimax.venta.engine.model.prefabs.LightPrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.joml.Vector3f;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class LightPrefabImplementation extends AbstractPrefabImplementation implements LightPrefab {
    @NonNull
    LightType type;

    @NonNull
    Vector3f color;

    @NonNull
    Vector3f direction;

    @NonNull
    Attenuation attenuation;

    float intensity;
    float range;
    boolean castShadows;
}