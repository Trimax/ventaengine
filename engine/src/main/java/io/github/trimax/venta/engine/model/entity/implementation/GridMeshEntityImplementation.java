package io.github.trimax.venta.engine.model.entity.implementation;

import org.joml.Vector2f;
import org.joml.Vector2i;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.entity.GridMeshEntity;
import io.github.trimax.venta.engine.model.prefabs.implementation.AbstractPrefabImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class GridMeshEntityImplementation extends AbstractPrefabImplementation implements GridMeshEntity {
    @NonNull
    Geometry geometry;

    /** Size of the grid in local units, the heightmap covers it */
    @NonNull
    Vector2f size;

    /** Number of grid cells along x and z */
    @NonNull
    Vector2i segments;
}
