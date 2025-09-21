package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.model.common.geo.Grid;
import io.github.trimax.venta.engine.model.common.geo.Wave;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.prefabs.GridMeshPrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class GridMeshPrefabImplementation extends AbstractPrefabImplementation implements GridMeshPrefab {
    @NonNull
    ProgramEntityImplementation program;

    List<Wave> waves;

    int verticesCount;
    int facetsCount;

    int vertexArrayObjectID;
    int verticesBufferID;
    int facetsBufferID;

    //TODO: Temporary
    public Grid grid;
}
