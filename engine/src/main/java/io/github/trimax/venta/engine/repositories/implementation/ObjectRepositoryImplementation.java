package io.github.trimax.venta.engine.repositories.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.hierarchy.LodLevel;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshPart;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.dto.ObjectDTO;
import io.github.trimax.venta.engine.model.dto.object.ObjectLodDTO;
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
        final var root = loadMeshHierarchy(objectDTO.root());

        // The levels are placed by the main model, or a simpler model would be scaled to its own size and jump when it replaces the main one
        final var lods = loadLevels(resourcePath, objectDTO.lods(), root);
        return abettor.createObject(programRegistry.get(objectDTO.program()),
                Optional.ofNullable(objectDTO.material()).map(materialRegistry::get).orElse(null),
                MeshUtil.normalize(root), lods);
    }

    private List<LodLevel> loadLevels(final String resourcePath, final List<ObjectLodDTO> levels, final Node<MeshReference> main) {
        if (levels == null || levels.isEmpty())
            return List.of();

        var previous = 1.f;
        final var result = new ArrayList<LodLevel>(levels.size());
        for (final var level : levels) {
            final var size = level.screenSize();
            if (!(size > 0.f && size < previous))
                throw new IllegalArgumentException(String.format("The levels of detail of %s must go from the nearest to the farthest: the screen size "
                        + "of a level must be greater than 0 and less than %s, got %s", resourcePath, previous, size));

            result.add(new LodLevel(MeshUtil.normalize(loadMeshHierarchy(level.root()), main), size, Boolean.TRUE.equals(level.facing())));
            previous = size;
        }

        return List.copyOf(result);
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
