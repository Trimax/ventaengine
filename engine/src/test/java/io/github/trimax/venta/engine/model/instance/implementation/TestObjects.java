package io.github.trimax.venta.engine.model.instance.implementation;

import java.util.List;

import org.joml.Vector3f;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.hierarchy.LodLevel;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TestMeshes;

/**
 * Objects for the tests of other packages: the constructors of the instances are not public.
 */
public final class TestObjects {
    private TestObjects() {
    }

    /** A model that is one mesh: a cube of the size 1 without a material of its own */
    public static Node<MeshReference> model() {
        return new Node<>("part", new MeshReference(TestMeshes.cube(1), null, new Transform()), List.of());
    }

    /** A level of detail that is a model of the size 1 */
    public static LodLevel level(final float screenSize) {
        return new LodLevel(model(), screenSize, false);
    }

    /** A level of detail that is a card turned to the camera */
    public static LodLevel card(final float screenSize) {
        return new LodLevel(model(), screenSize, true);
    }

    /** An object with a cube as the main model and levels of detail */
    public static ObjectInstanceImplementation object(final MaterialEntityImplementation material, final LodLevel... levels) {
        final var gizmo = new GizmoInstanceImplementation("gizmo", TestMeshes.program(), TestMeshes.cube(1),
                new Vector3f(), new Vector3f(), new Vector3f(1));

        return new ObjectInstanceImplementation("object", TestMeshes.program(), material, model(), List.of(levels), gizmo);
    }
}
