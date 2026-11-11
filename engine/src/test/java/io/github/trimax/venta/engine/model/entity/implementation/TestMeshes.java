package io.github.trimax.venta.engine.model.entity.implementation;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.common.geo.Geometry;

/** Creates mesh entities without GPU resources for tests */
public final class TestMeshes {
    private TestMeshes() {
    }

    /** A mesh whose bounding box is the cube [-0.5, 0.5] scaled by the given size */
    public static MeshEntityImplementation cube(final float size) {
        return mesh(new Vector3f(-0.5f * size), new Vector3f(0.5f * size));
    }

    public static MeshEntityImplementation mesh(final Vector3f min, final Vector3f max) {
        return new MeshEntityImplementation(new Geometry(0, null, null, null), new BoundingBox(min, max));
    }
}
