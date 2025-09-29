package io.github.trimax.venta.engine.repositories.implementation;

import java.util.Optional;

import org.joml.Vector3f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.model.common.scene.SceneBillboard;
import io.github.trimax.venta.engine.model.common.scene.SceneEmitter;
import io.github.trimax.venta.engine.model.common.scene.SceneLight;
import io.github.trimax.venta.engine.model.common.scene.SceneObject;
import io.github.trimax.venta.engine.model.common.scene.SceneSoundSource;
import io.github.trimax.venta.engine.model.common.shared.DirectionalLight;
import io.github.trimax.venta.engine.model.common.shared.Fog;
import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.engine.model.dto.common.ColorDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneBillboardDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneEmitterDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneLightDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneObjectDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneSoundSourceDTO;
import io.github.trimax.venta.engine.model.prefabs.ScenePrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.Abettor;
import io.github.trimax.venta.engine.model.prefabs.implementation.ScenePrefabImplementation;
import io.github.trimax.venta.engine.registries.implementation.CubemapRegistryImplementation;
import io.github.trimax.venta.engine.repositories.SceneRepository;
import io.github.trimax.venta.engine.services.ResourceService;
import io.github.trimax.venta.engine.utils.TransformationUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneRepositoryImplementation
        extends AbstractRepositoryImplementation<ScenePrefabImplementation, ScenePrefab>
        implements SceneRepository {
    private final SoundSourceRepositoryImplementation soundSourceRepository;
    private final BillboardRepositoryImplementation billboardRepository;
    private final EmitterRepositoryImplementation emitterRepository;
    private final ObjectRepositoryImplementation objectRepository;
    private final LightRepositoryImplementation lightRepository;

    private final CubemapRegistryImplementation cubemapRegistry;
    private final ResourceService resourceService;
    private final Abettor abettor;

    @Override
    protected ScenePrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading scene {}", resourcePath);

        final var sceneDTO = resourceService.getAsObject(String.format("/scenes/%s", resourcePath), SceneDTO.class);

        return abettor.createScene(
                TransformationUtil.transform(sceneDTO.lights(), this::createLight),
                TransformationUtil.transform(sceneDTO.objects(), this::createObject),
                TransformationUtil.transform(sceneDTO.emitters(), this::createEmitter),
                TransformationUtil.transform(sceneDTO.billboards(), this::createBillboard),
                TransformationUtil.transform(sceneDTO.soundSources(), this::createSoundSource),
                Optional.ofNullable(sceneDTO.skybox()).map(cubemapRegistry::get).orElse(null),
                Optional.ofNullable(sceneDTO.directionalLight()).map(DirectionalLight::new).orElse(null),
                Optional.ofNullable(sceneDTO.ambientLight()).map(ColorDTO::toVector3f).orElse(new Vector3f(DefinitionsCommon.VECTOR3F_ONE)),
                Optional.ofNullable(sceneDTO.fog()).map(Fog::new).orElse(null));
    }

    private SceneLight createLight(@NonNull final SceneLightDTO dto) {
        return new SceneLight(dto, lightRepository.get(dto.light()));
    }

    private SceneObject createObject(@NonNull final SceneObjectDTO dto) {
        return new SceneObject(dto, objectRepository.get(dto.object()));
    }

    private SceneEmitter createEmitter(@NonNull final SceneEmitterDTO dto) {
        return new SceneEmitter(dto, emitterRepository.get(dto.emitter()));
    }

    private SceneBillboard createBillboard(@NonNull final SceneBillboardDTO dto) {
        return new SceneBillboard(dto, billboardRepository.get(dto.billboard()));
    }

    private SceneSoundSource createSoundSource(@NonNull final SceneSoundSourceDTO dto) {
        return new SceneSoundSource(dto, soundSourceRepository.get(dto.sourceSource()));
    }

    @Override
    protected void unload(@NonNull final ScenePrefabImplementation entity) {
        log.info("Unloading scene {}", entity.getID());
    }
}
