package io.github.trimax.venta.engine.helpers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.MaterialManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.MeshManagerImplementation;
import io.github.trimax.venta.engine.model.dto.ObjectMeshDTO;
import io.github.trimax.venta.engine.model.entity.MeshInstance;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MeshHelper {
    private final MaterialManagerImplementation materialManager;
    private final MeshManagerImplementation meshManager;

    public MeshInstance build(@NonNull final ObjectMeshDTO meshDTO) {
        final var mesh = meshManager.load(meshDTO.name());
        mesh.setMaterial(materialManager.load(meshDTO.material()));

        //TODO: Set transformation matrix
        if (CollectionUtils.isNotEmpty(meshDTO.children()))
            mesh.getChildren().addAll(StreamEx.of(meshDTO.children()).map(this::build).toList());

        return mesh;
    }
}
