package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.model.entity.implementation.SoundEntityImplementation;
import io.github.trimax.venta.engine.model.prefabs.SoundSourcePrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class SoundSourcePrefabImplementation extends AbstractPrefabImplementation implements SoundSourcePrefab {
    @NonNull
    SoundEntityImplementation sound;
    float volume;
    float pitch;
    boolean looping;
}