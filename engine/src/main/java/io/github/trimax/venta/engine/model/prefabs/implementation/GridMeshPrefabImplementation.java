package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.prefabs.GridMeshPrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class GridMeshPrefabImplementation extends AbstractPrefabImplementation implements GridMeshPrefab {
    ProgramEntity program;

    int verticesCount;
    int facetsCount;

    int vertexArrayObjectID;
    int verticesBufferID;
    int facetsBufferID;
}
