package io.github.trimax.venta.engine.repositories.implementation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshPart;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.dto.ObjectDTO;
import io.github.trimax.venta.engine.model.dto.object.ObjectMeshDTO;
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
    
    private Node<MeshReference> loadMeshHierarchy(@NonNull final Node<ObjectMeshDTO> node) {
        final var name = Optional.ofNullable(node.name()).orElse(UUID.randomUUID().toString());
        final var children = node.hasChildren() ? StreamEx.of(node.children()).map(this::loadMeshHierarchy).toList() : null;

        final var value = node.value();
        if (value == null)
            return new Node<>(name, null, children);

        // A model without a material of its own is split into its parts with the materials of the model file
        final var parts = value.material() == null ? meshRegistry.getParts(value.mesh()) : List.<MeshPart>of();
        if (parts.size() > 1 || (parts.size() == 1 && parts.getFirst().hasMaterial()))
            return new Node<>(name, new MeshReference(null, null, value.transform()),
                    StreamEx.of(parts).map(this::convert).append(children == null ? List.of() : children).toList());

        return new Node<>(name, convert(value), children);
    }

    private Node<MeshReference> convert(@NonNull final MeshPart part) {
        final var material = part.hasMaterial() ? materialRegistry.get(part.materialLibraries(), part.material()) : null;
        return new Node<>(part.name(), new MeshReference(part.mesh(), material, new Transform()), null);
    }

    private MeshReference convert(@NonNull final ObjectMeshDTO value) {
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
