package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.MeshEntity;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.geo.Node;
import io.github.trimax.venta.engine.model.math.Transform;
import io.github.trimax.venta.engine.model.prefabs.ObjectPrefab;
import lombok.Value;

@Value
public class ObjectPrefabImplementation extends AbstractPrefabImplementation implements ObjectPrefab {
    ProgramEntity program;
    Node<MeshReference> root;

    public record MeshReference(MeshEntity mesh, MaterialEntity material, Transform transform) {}
}
