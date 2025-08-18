package io.github.trimax.venta.engine.model.parameters;

import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.light.Attenuation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.joml.Vector3f;

@Getter
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LightParameters implements AbstractParameters {
    @NonNull
    LightType type;

    @NonNull
    Vector3f color;

    float intensity;

    Attenuation attenuation;

    float range;

    boolean castShadows;
}
