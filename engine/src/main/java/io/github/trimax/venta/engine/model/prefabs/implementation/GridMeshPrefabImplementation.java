package io.github.trimax.venta.engine.model.prefabs.implementation;

import java.util.List;

import io.github.trimax.venta.engine.model.common.geo.Grid;
import io.github.trimax.venta.engine.model.common.geo.Wave;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.prefabs.GridMeshPrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class GridMeshPrefabImplementation extends AbstractPrefabImplementation implements GridMeshPrefab {
    ProgramEntity program;
    List<Wave> waves;
    Wave wave;

    int verticesCount;
    int facetsCount;

    int vertexArrayObjectID;
    int verticesBufferID;
    int facetsBufferID;

    //TODO: Temporary
    public Grid grid;
}
