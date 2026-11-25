package io.github.trimax.venta.engine.model.prefabs.implementation;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.shared.Attenuation;
import io.github.trimax.venta.engine.model.common.shared.Cone;
import io.github.trimax.venta.engine.model.prefabs.LightPrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class LightPrefabImplementation extends AbstractPrefabImplementation implements LightPrefab {
    @NonNull
    Vector3f color;

    @NonNull
    Attenuation attenuation;

    boolean castShadows;
    float intensity;
    float range;

    @NonNull
    LightType type;

    /** The cone of a spot light, null for a point light */
    Cone cone;
}