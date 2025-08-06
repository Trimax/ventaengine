package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.hierarchy.Node;
import io.github.trimax.venta.engine.model.prefabs.ObjectPrefab;
import lombok.Value;

@Value
public class ObjectPrefabImplementation extends AbstractPrefabImplementation implements ObjectPrefab {
    ProgramEntity program;
    Node<MeshReference> root;
}
