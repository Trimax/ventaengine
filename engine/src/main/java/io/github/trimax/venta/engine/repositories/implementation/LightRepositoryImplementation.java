package io.github.trimax.venta.engine.repositories.implementation;

import java.util.Optional;

import org.joml.Vector3f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.model.common.shared.Attenuation;
import io.github.trimax.venta.engine.model.dto.LightDTO;
import io.github.trimax.venta.engine.model.dto.common.ColorDTO;
import io.github.trimax.venta.engine.model.prefabs.LightPrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.Abettor;
import io.github.trimax.venta.engine.model.prefabs.implementation.LightPrefabImplementation;
import io.github.trimax.venta.engine.repositories.LightRepository;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LightRepositoryImplementation
        extends AbstractRepositoryImplementation<LightPrefabImplementation, LightPrefab>
        implements LightRepository {
    private final ResourceService resourceService;
    private final Abettor abettor;

    @Override
    protected LightPrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading light {}", resourcePath);

        final var lightDTO = resourceService.getAsObject(String.format("/lights/%s", resourcePath), LightDTO.class);
        return abettor.createLight(
                Optional.of(lightDTO.color()).map(ColorDTO::toVector3f).orElse(new Vector3f(DefinitionsCommon.VECTOR3F_ONE)),
                Optional.ofNullable(lightDTO.attenuation()).map(Attenuation::new).orElse(new Attenuation()),
                lightDTO.castShadows(),
                lightDTO.intensity(),
                lightDTO.range());
    }

    @Override
    protected void unload(@NonNull final LightPrefabImplementation entity) {
        log.info("Unloading light {}", entity.getID());
    }
}
