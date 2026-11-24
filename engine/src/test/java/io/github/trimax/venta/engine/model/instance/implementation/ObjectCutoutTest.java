package io.github.trimax.venta.engine.model.instance.implementation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TestMeshes;
import io.github.trimax.venta.engine.utils.ParsingUtil;

/**
 * Whether an object has a cutout surface decides which shader draws it, so it is looked for in the material of the
 * object and in the materials of the parts of its model.
 */
final class ObjectCutoutTest {
    private static MaterialEntityImplementation material(final String mode) throws Exception {
        final var constructor = MaterialEntityImplementation.class.getDeclaredConstructor(MaterialDTO.class);
        constructor.setAccessible(true);

        return constructor.newInstance(ParsingUtil.asObject("{\"alphaMode\": \"" + mode + "\"}", MaterialDTO.class));
    }

    private static Node<MeshReference> leaf(final String name, final MaterialEntityImplementation material) {
        return new Node<>(name, new MeshReference(TestMeshes.cube(1), material, new Transform()), List.of());
    }

    private static ObjectInstanceImplementation object(final MaterialEntityImplementation material, final Node<MeshReference> mesh) {
        final var gizmo = new GizmoInstanceImplementation("gizmo", TestMeshes.program(), TestMeshes.cube(1),
                new Vector3f(), new Vector3f(), new Vector3f(1));

        return new ObjectInstanceImplementation("object", TestMeshes.program(), material, mesh, gizmo);
    }

    @Test
    void testObjectWithoutCutoutMaterialHasNoCutout() throws Exception {
        assertFalse(object(null, leaf("part", null)).hasCutout(), "No material at all");
        assertFalse(object(material("blend"), leaf("part", material("blend"))).hasCutout(), "Blend materials");
    }

    @Test
    void testMaterialOfTheObjectDecides() throws Exception {
        assertTrue(object(material("cutout"), leaf("part", null)).hasCutout());
    }

    @Test
    void testMaterialOfAPartDecides() throws Exception {
        final var mesh = new Node<>("root", (MeshReference) null, List.of(leaf("trunk", material("blend")), leaf("leaves", material("cutout"))));

        assertTrue(object(null, mesh).hasCutout(), "A cutout part makes the object cutout");
    }

    @Test
    void testMaterialOfADeepPartDecides() throws Exception {
        final var deep = new Node<>("branch", (MeshReference) null, List.of(leaf("leaf", material("cutout"))));
        final var mesh = new Node<>("root", (MeshReference) null, List.of(new Node<>("trunk", (MeshReference) null, List.of(deep))));

        assertTrue(object(null, mesh).hasCutout());
    }

    @Test
    void testNewMaterialChangesTheAnswer() throws Exception {
        final var object = object(material("blend"), leaf("part", null));
        assertFalse(object.hasCutout());

        object.setMaterial(material("cutout"));
        assertTrue(object.hasCutout(), "The answer is not remembered across a new material");

        object.setMaterial(material("blend"));
        assertFalse(object.hasCutout());
    }
}
