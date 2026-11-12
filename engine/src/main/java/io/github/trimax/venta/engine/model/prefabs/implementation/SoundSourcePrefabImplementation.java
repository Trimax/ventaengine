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

    /** The sound has its full volume up to this distance from the listener, then it fades */
    float referenceDistance;

    /** Beyond this distance the sound doesn't fade anymore */
    float maximalDistance;

    /** How fast the sound fades with the distance: 0 is no fading, 1 is the physical fading */
    float rolloff;
}