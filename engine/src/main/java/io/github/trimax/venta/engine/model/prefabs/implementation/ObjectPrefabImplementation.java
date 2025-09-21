package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.prefabs.ObjectPrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ObjectPrefabImplementation extends AbstractPrefabImplementation implements ObjectPrefab {
    ProgramEntityImplementation program;
    MaterialEntityImplementation material;
    Node<MeshReference> root;
}
