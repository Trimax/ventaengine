package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.managers.ObjectManager;
import io.github.trimax.venta.engine.model.dto.ObjectDTO;
import io.github.trimax.venta.engine.model.dto.ObjectMeshDTO;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.instance.MeshInstance;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import io.github.trimax.venta.engine.model.instance.implementation.MeshInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectManagerImplementation
        extends AbstractManagerImplementation<ObjectInstanceImplementation, ObjectInstance>
        implements ObjectManager {
    private final ProgramRegistryImplementation programRegistry;
    private final MaterialManagerImplementation materialManager;
    private final GizmoManagerImplementation gizmoManager;
    private final MeshManagerImplementation meshManager;

    @Override
    public ObjectInstance create(@NonNull final String name,
                                 @NonNull final MeshInstance mesh,
                                 @NonNull final ProgramEntity program) {
        log.info("Creating object {}", name);

        return store(new ObjectInstanceImplementation(name, program,
                meshManager.getInstance(mesh.getID()),
                gizmoManager.create("Bounding box", GizmoType.Object)));
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

    private MeshInstanceImplementation buildMeshHierarchy(@NonNull final ObjectMeshDTO meshDTO) {
        final var mesh = meshManager.load(meshDTO.name());
        mesh.setMaterial(materialManager.load(meshDTO.material()));

        //TODO: Set transformation matrix
        if (CollectionUtils.isNotEmpty(meshDTO.children()))
            StreamEx.of(meshDTO.children()).map(this::buildMeshHierarchy).forEach(mesh::addChild);

        return mesh;
    }

    @Override
    protected void destroy(final ObjectInstanceImplementation object) {
        log.info("Destroying object {} ({})", object.getID(), object.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }
}
