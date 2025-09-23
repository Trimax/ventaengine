package io.github.trimax.venta.engine.repositories.implementation;

import java.util.Optional;
import java.util.UUID;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.dto.MeshDTO;
import io.github.trimax.venta.engine.model.dto.ObjectDTO;
import io.github.trimax.venta.engine.model.prefabs.ObjectPrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.Abettor;
import io.github.trimax.venta.engine.model.prefabs.implementation.ObjectPrefabImplementation;
import io.github.trimax.venta.engine.registries.implementation.MaterialRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.MeshRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.repositories.ObjectRepository;
import io.github.trimax.venta.engine.services.ResourceService;
import io.github.trimax.venta.engine.utils.MeshUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectRepositoryImplementation
        extends AbstractRepositoryImplementation<ObjectPrefabImplementation, ObjectPrefab> 
        implements ObjectRepository {
    private final MaterialRegistryImplementation materialRegistry;
    private final ProgramRegistryImplementation programRegistry;
    private final MeshRegistryImplementation meshRegistry;
    private final ResourceService resourceService;
    private final Abettor abettor;
    
    @Override
    protected ObjectPrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading object {}", resourcePath);

        final var objectDTO = resourceService.getAsObject(String.format("/objects/%s", resourcePath), ObjectDTO.class);
        return abettor.createObject(programRegistry.get(objectDTO.program()),
                Optional.ofNullable(objectDTO.material()).map(materialRegistry::get).orElse(null),
                MeshUtil.normalize(loadMeshHierarchy(objectDTO.root())));
    }
    
    private Node<MeshReference> loadMeshHierarchy(@NonNull final Node<MeshDTO> node) {
        return new Node<>(Optional.ofNullable(node.name()).orElse(UUID.randomUUID().toString()),
                Optional.ofNullable(node.value()).map(this::convert).orElse(null),
                node.hasChildren() ? StreamEx.of(node.children()).map(this::loadMeshHierarchy).toList() : null);
    }

    private MeshReference convert(@NonNull final MeshDTO value) {
        return new MeshReference(
                Optional.ofNullable(value.mesh()).map(meshRegistry::get).orElse(null),
                Optional.ofNullable(value.material()).map(materialRegistry::get).orElse(null),
                value.transform()
        );
    }

    @Override
    protected void unload(@NonNull final ObjectPrefabImplementation entity) {
        log.info("Unloading object {}", entity.getID());
    }
}
