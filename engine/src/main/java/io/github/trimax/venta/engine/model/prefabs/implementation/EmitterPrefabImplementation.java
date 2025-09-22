package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.model.common.dto.Range;
import io.github.trimax.venta.engine.model.common.dto.Variable;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import io.github.trimax.venta.engine.model.prefabs.EmitterPrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.joml.Vector3f;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class EmitterPrefabImplementation extends AbstractPrefabImplementation implements EmitterPrefab {
    @NonNull
    TextureEntityImplementation texture;

    @NonNull
    Range<Float> particleSize;

    @NonNull
    Variable<Float> particleLifetime;

    @NonNull
    Variable<Vector3f> particleVelocity;

    @NonNull
    Variable<Float> particleAngularVelocity;

    int particlesCount;
    float emissionRate;
}
