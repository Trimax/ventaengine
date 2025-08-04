package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.managers.ObjectManager;
import io.github.trimax.venta.engine.model.dto.ObjectDTO;
import io.github.trimax.venta.engine.model.dto.ObjectMeshDTO;
import io.github.trimax.venta.engine.model.entity.MeshEntity;
import io.github.trimax.venta.engine.model.entity.ObjectEntity;
import io.github.trimax.venta.engine.model.view.MeshView;
import io.github.trimax.venta.engine.model.view.ObjectView;
import io.github.trimax.venta.engine.model.view.ProgramView;
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
        extends AbstractManagerImplementation<ObjectEntity, ObjectView>
        implements ObjectManager {
    private final MaterialManagerImplementation materialManager;
    private final ResourceManagerImplementation resourceManager;
    private final ProgramManagerImplementation programManager;
    private final GizmoManagerImplementation gizmoManager;
    private final MeshManagerImplementation meshManager;

    @Override
    public ObjectView create(@NonNull final String name,
                             @NonNull final MeshView mesh,
                             @NonNull final ProgramView program) {
        log.info("Creating object {}", name);

        return store(new ObjectEntity(name,
                programManager.getEntity(program.getID()),
                meshManager.getEntity(mesh.getID()),
                gizmoManager.create("Bounding box", GizmoType.Object)));
    }

    @Override
    public ObjectView load(@NonNull final String name) {
        log.info("Loading object {}", name);

        final var objectDTO = resourceManager.load(String.format("/objects/%s.json", name), ObjectDTO.class);
        return store(new ObjectEntity(name,
                programManager.load(objectDTO.program()),
                buildMeshHierarchy(objectDTO.mesh()),
                gizmoManager.create("Bounding box", GizmoType.Object)));
    }

    private MeshEntity buildMeshHierarchy(@NonNull final ObjectMeshDTO meshDTO) {
        final var mesh = meshManager.load(meshDTO.name());
        mesh.setMaterial(materialManager.load(meshDTO.material()));

        //TODO: Set transformation matrix
        if (CollectionUtils.isNotEmpty(meshDTO.children()))
            StreamEx.of(meshDTO.children()).map(this::buildMeshHierarchy).forEach(mesh::addChild);

        return mesh;
    }

    @Override
    protected void destroy(final ObjectEntity object) {
        log.info("Destroying object {} ({})", object.getID(), object.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }
}
