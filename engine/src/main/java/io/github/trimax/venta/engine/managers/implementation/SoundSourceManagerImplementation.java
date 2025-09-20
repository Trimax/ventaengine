package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.exceptions.UnknownInstanceException;
import io.github.trimax.venta.engine.managers.SoundSourceManager;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.instance.SoundSourceInstance;
import io.github.trimax.venta.engine.model.instance.implementation.Abettor;
import io.github.trimax.venta.engine.model.instance.implementation.SoundSourceInstanceImplementation;
import io.github.trimax.venta.engine.model.prefabs.SoundSourcePrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.SoundSourcePrefabImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SoundSourceManagerImplementation
        extends AbstractManagerImplementation<SoundSourceInstanceImplementation, SoundSourceInstance>
        implements SoundSourceManager {
    private final GizmoManagerImplementation gizmoManager;
    private final Abettor abettor;
    private final Memory memory;

    @Override
    public SoundSourceInstanceImplementation create(@NonNull final String name, @NonNull final SoundSourcePrefab prefab) {
        if (prefab instanceof SoundSourcePrefabImplementation sound)
            return create(name, sound);

        throw new UnknownInstanceException(prefab.getClass());
    }

    private SoundSourceInstanceImplementation create(@NonNull final String name,
                                                     @NonNull final SoundSourcePrefabImplementation prefab) {
        log.info("Loading sound {}", name);

        return store(abettor.createSound(
                name,
                prefab.getSound(),
                Optional.of(prefab.getVolume()).orElse(1.0f),
                Optional.of(prefab.getPitch()).orElse(1.0f),
                Optional.of(prefab.isLooping()).orElse(false),
                memory.getAudioSources().create("Sound source %s", name),
                gizmoManager.create("Sound box", GizmoType.Light)
        ));
    }

    @Override
    public void delete(@NonNull final SoundSourceInstance instance) {
        if (instance instanceof SoundSourceInstanceImplementation sound)
            super.delete(sound);
    }

    @Override
    protected void destroy(final SoundSourceInstanceImplementation sound) {
        log.info("Destroying sound {} ({})", sound.getID(), sound.getName());

        if (sound.isPlaying())
            sound.stop();

        memory.getAudioSources().delete(sound.getSourceID());
    }
}
