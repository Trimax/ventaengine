package io.github.trimax.venta.engine.repositories.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.dto.SoundSourceDTO;
import io.github.trimax.venta.engine.model.prefabs.SoundSourcePrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.Abettor;
import io.github.trimax.venta.engine.model.prefabs.implementation.SoundSourcePrefabImplementation;
import io.github.trimax.venta.engine.registries.implementation.SoundRegistryImplementation;
import io.github.trimax.venta.engine.repositories.SoundSourceRepository;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SoundSourceRepositoryImplementation
        extends AbstractRepositoryImplementation<SoundSourcePrefabImplementation, SoundSourcePrefab>
        implements SoundSourceRepository {
    private final SoundRegistryImplementation soundRegistry;
    private final ResourceService resourceService;
    private final Abettor abettor;

    @Override
    protected SoundSourcePrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading sound {}", resourcePath);
        final var soundSourceDTO = resourceService.getAsObject(String.format("/soundsources/%s", resourcePath), SoundSourceDTO.class);

        if (soundSourceDTO == null)
            throw new IllegalStateException(String.format("Sound source %s could not be loaded", resourcePath));

        return abettor.createSound(
                soundRegistry.get(soundSourceDTO.sound()),
                Optional.ofNullable(soundSourceDTO.volume()).orElse(1.0f),
                Optional.ofNullable(soundSourceDTO.pitch()).orElse(0.0f),
                Optional.ofNullable(soundSourceDTO.looping()).orElse(true)
        );
    }

    @Override
    protected void unload(@NonNull final SoundSourcePrefabImplementation entity) {
        log.info("Unloading sound {}", entity.getID());
    }
}
