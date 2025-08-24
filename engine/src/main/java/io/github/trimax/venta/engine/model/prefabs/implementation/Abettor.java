package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.dto.LightPrefabDTO;
import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Abettor {
    public LightPrefabImplementation createLight(@NonNull final LightPrefabDTO dto) {
        return new LightPrefabImplementation(dto);
    }

    public ObjectPrefabImplementation createObject(final ProgramEntity program,
                                                   final Node<MeshReference> root) {
        return new ObjectPrefabImplementation(program, root);
    }

    public ScenePrefabImplementation createScene(@NonNull final SceneDTO dto) {
        return new ScenePrefabImplementation(dto);
    }
}
