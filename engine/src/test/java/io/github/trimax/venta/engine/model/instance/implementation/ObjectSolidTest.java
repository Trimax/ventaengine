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
 * An object is solid when no surface of it mixes colors: it is drawn before the others, in any order, and with instancing.
 */
final class ObjectSolidTest {
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
    void testOpaqueAndCutoutObjectsAreSolid() throws Exception {
        assertTrue(object(material("opaque"), leaf("part", null)).isSolid());
        assertTrue(object(material("cutout"), leaf("part", null)).isSolid());
        assertTrue(object(null, leaf("part", material("opaque"))).isSolid(), "The material of the part counts");
    }

    @Test
    void testObjectsThatMixColorsAreNotSolid() throws Exception {
        assertFalse(object(material("blend"), leaf("part", null)).isSolid());
        assertFalse(object(null, leaf("part", null)).isSolid(), "A surface without a material mixes colors");
        assertFalse(object(material("opaque"), leaf("part", material("blend"))).isSolid(), "The part is glass");
    }

    @Test
    void testOneSurfaceThatMixesColorsMakesTheObjectNotSolid() throws Exception {
        final var mesh = new Node<>("root", new MeshReference(null, null, new Transform()),
                List.of(leaf("wall", material("opaque")), leaf("window", material("blend"))));

        assertFalse(object(null, mesh).isSolid());
    }

    @Test
    void testAllPartsOpaqueMakeTheObjectSolid() throws Exception {
        final var mesh = new Node<>("root", new MeshReference(null, null, new Transform()),
                List.of(leaf("wall", material("opaque")), leaf("roof", material("opaque")), leaf("leaves", material("cutout"))));

        assertTrue(object(null, mesh).isSolid());
    }

    @Test
    void testACutoutPartMakesTheObjectSolidAsBefore() throws Exception {
        final var mesh = new Node<>("root", new MeshReference(null, null, new Transform()),
                List.of(leaf("trunk", material("blend")), leaf("leaves", material("cutout"))));
        final var object = object(null, mesh);

        assertTrue(object.hasCutout());
        assertTrue(object.isSolid(), "The trunk of a tree with leaves is drawn without sorting, as it always was");
    }

    @Test
    void testTheAnswerChangesWithTheMaterialOfTheObject() throws Exception {
        final var object = object(material("blend"), leaf("part", null));
        assertFalse(object.isSolid());

        object.setMaterial(material("opaque"));
        assertTrue(object.isSolid(), "The answer is not remembered across a new material");

        object.setMaterial(material("blend"));
        assertFalse(object.isSolid());
    }
}
