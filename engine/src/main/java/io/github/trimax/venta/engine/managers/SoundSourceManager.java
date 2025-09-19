package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.SoundSourceInstance;
import io.github.trimax.venta.engine.model.prefabs.SoundSourcePrefab;
import lombok.NonNull;

public interface SoundSourceManager extends AbstractManager<SoundSourceInstance> {
    SoundSourceInstance create(@NonNull final String name,
                               @NonNull final SoundSourcePrefab prefab);

    void delete(@NonNull final SoundSourceInstance instance);
}
