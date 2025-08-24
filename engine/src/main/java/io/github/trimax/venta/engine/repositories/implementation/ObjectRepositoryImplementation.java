package io.github.trimax.venta.engine.repositories.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.dto.MeshPrefabDTO;
import io.github.trimax.venta.engine.model.dto.ObjectPrefabDTO;
import io.github.trimax.venta.engine.model.prefabs.ObjectPrefab;
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

import java.util.Optional;

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
    
    @Override
    protected ObjectPrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading object {}", resourcePath);

        final var objectDTO = resourceService.getAsObject(String.format("/objects/%s", resourcePath), ObjectPrefabDTO.class);
        return new ObjectPrefabImplementation(programRegistry.get(objectDTO.program()), MeshUtil.normalize(loadMeshHierarchy(objectDTO.root())));
    }
    
    private Node<MeshReference> loadMeshHierarchy(@NonNull final Node<MeshPrefabDTO> node) {
        return new Node<>(node.name(), convert(node.value()),
                node.hasChildren() ? StreamEx.of(node.children()).map(this::loadMeshHierarchy).toList() : null);
    }

    private MeshReference convert(@NonNull final MeshPrefabDTO value) {
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
