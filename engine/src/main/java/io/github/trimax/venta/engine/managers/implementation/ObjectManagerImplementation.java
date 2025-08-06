package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.exceptions.UnknownInstanceException;
import io.github.trimax.venta.engine.managers.ObjectManager;
import io.github.trimax.venta.engine.model.dto.ObjectDTO;
import io.github.trimax.venta.engine.model.dto.ObjectMeshDTO;
import io.github.trimax.venta.engine.model.entity.MeshPrefab;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.entity.implementation.MeshPrefabImplementation;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;
import io.github.trimax.venta.engine.registries.implementation.MaterialRegistryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.MeshRepositoryImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectManagerImplementation
        extends AbstractManagerImplementation<ObjectInstanceImplementation, ObjectInstance>
        implements ObjectManager {
    private final MaterialRegistryImplementation materialRegistry;
    private final ProgramRegistryImplementation programRegistry;
    private final GizmoManagerImplementation gizmoManager;

    private final MeshRepositoryImplementation meshRegistry;

    @Override
    public ObjectInstance create(@NonNull final String name,
                                 @NonNull final MeshPrefab mesh,
                                 @NonNull final ProgramEntity program) {
        log.info("Creating object {}", name);

        if (mesh instanceof MeshPrefabImplementation entity)
            return store(new ObjectInstanceImplementation(name, program, entity,
                gizmoManager.create("Bounding box", GizmoType.Object)));

        throw new UnknownInstanceException(mesh.getClass());
    }

    @Override
    public ObjectInstance load(@NonNull final String name) {
        log.info("Loading object {}", name);

        final var objectDTO = ResourceUtil.loadAsObject(String.format("/objects/%s.json", name), ObjectDTO.class);
        return store(new ObjectInstanceImplementation(name,
                programRegistry.get(objectDTO.program()),
                buildMeshHierarchy(objectDTO.mesh()),
                gizmoManager.create("Bounding box", GizmoType.Object)));
    }

    private MeshPrefabImplementation buildMeshHierarchy(@NonNull final ObjectMeshDTO meshDTO) {
        final var mesh = meshRegistry.get(meshDTO.name());
        mesh.setMaterial(materialRegistry.get(meshDTO.material()));

        return mesh;
    }

    @Override
    protected void destroy(final ObjectInstanceImplementation object) {
        log.info("Destroying object {} ({})", object.getID(), object.getName());
    }
}
