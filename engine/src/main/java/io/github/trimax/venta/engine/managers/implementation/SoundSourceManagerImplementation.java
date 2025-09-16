package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.exceptions.UnknownInstanceException;
import io.github.trimax.venta.engine.managers.SoundSourceManager;
import io.github.trimax.venta.engine.model.instance.SoundSourceInstance;
import io.github.trimax.venta.engine.model.instance.implementation.Abettor;
import io.github.trimax.venta.engine.model.instance.implementation.SoundSourceInstanceImplementation;
import io.github.trimax.venta.engine.model.prefabs.SoundSourcePrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.SoundSourcePrefabImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SoundSourceManagerImplementation
        extends AbstractManagerImplementation<SoundSourceInstanceImplementation, SoundSourceInstance>
        implements SoundSourceManager {
    private final Abettor abettor;

    @Override
    public SoundSourceInstanceImplementation create(@NonNull final String name, @NonNull final SoundSourcePrefab prefab) {
        if (prefab instanceof SoundSourcePrefabImplementation sound)
            return create(name, sound);

        throw new UnknownInstanceException(prefab.getClass());
    }

    private SoundSourceInstanceImplementation create(@NonNull final String name, @NonNull final SoundSourcePrefabImplementation prefab) {
        log.info("Loading sound {}", name);

        return store(abettor.createSound(name, prefab));
    }

    @Override
    public void delete(@NonNull final SoundSourceInstance instance) {
        if (instance instanceof SoundSourceInstanceImplementation sound)
            super.delete(sound);
    }

    @Override
    protected void destroy(final SoundSourceInstanceImplementation sound) {
        log.info("Destroying sound {} ({})", sound.getID(), sound.getName());
    }
}
