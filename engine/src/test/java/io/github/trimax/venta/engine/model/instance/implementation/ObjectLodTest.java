package io.github.trimax.venta.engine.model.instance.implementation;

import static io.github.trimax.venta.engine.model.instance.implementation.TestObjects.card;
import static io.github.trimax.venta.engine.model.instance.implementation.TestObjects.level;
import static io.github.trimax.venta.engine.model.instance.implementation.TestObjects.object;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.common.hierarchy.LodLevel;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TestMeshes;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class ObjectLodTest {
    private static final float DURATION = 0.4f;
    private static final float EPSILON = 1e-6f;

    private static MaterialEntityImplementation material(final String mode) throws Exception {
        final var constructor = MaterialEntityImplementation.class.getDeclaredConstructor(MaterialDTO.class);
        constructor.setAccessible(true);

        return constructor.newInstance(ParsingUtil.asObject("{\"alphaMode\": \"" + mode + "\"}", MaterialDTO.class));
    }

    @Test
    void testObjectWithoutSimplerModelsHasOneLevel() {
        final var object = object(null);

        assertFalse(object.hasLods());
        assertEquals(1, object.getLodCount());
        assertEquals(0, object.getLod());
        assertSame(object.getMesh(), object.getMesh(0));
    }

    @Test
    void testLevelsFollowTheMainModel() {
        final var first = level(0.3f);
        final var second = level(0.1f);
        final var object = object(null, first, second);

        assertTrue(object.hasLods());
        assertEquals(3, object.getLodCount());
        assertSame(object.getMesh(), object.getMesh(0));
        assertSame(first.root(), object.getMesh(1));
        assertSame(second.root(), object.getMesh(2));
        assertArrayEquals(new float[]{0.3f, 0.1f}, object.getLodSizes());
    }

    @Test
    void testChangeOfTheLevelFadesTheOldOneOut() {
        final var object = object(null, level(0.3f));

        object.setLod(1);

        assertEquals(1, object.getLod());
        assertEquals(0, object.getPreviousLod(), "The old level is still drawn");
        assertEquals(0.f, object.getLodBlend(), EPSILON);

        object.advanceLod(0.1f, DURATION);
        assertEquals(0.25f, object.getLodBlend(), EPSILON);
        assertEquals(0, object.getPreviousLod());

        object.advanceLod(0.3f, DURATION);
        assertEquals(1.f, object.getLodBlend(), EPSILON);
        assertEquals(-1, object.getPreviousLod(), "The old level is dropped when it has faded out");
    }

    @Test
    void testSameLevelChangesNothing() {
        final var object = object(null, level(0.3f));

        object.setLod(0);

        assertEquals(-1, object.getPreviousLod());
        assertEquals(1.f, object.getLodBlend());
    }

    @Test
    void testChangeInTheMiddleOfAFadeStartsANewOne() {
        final var object = object(null, level(0.3f), level(0.1f));
        object.setLod(1);
        object.advanceLod(0.2f, DURATION);

        object.setLod(2);

        assertEquals(2, object.getLod());
        assertEquals(1, object.getPreviousLod(), "The level that was drawn last fades out");
        assertEquals(0.f, object.getLodBlend(), EPSILON);
    }

    @Test
    void testFadeWithoutDurationIsInstant() {
        final var object = object(null, level(0.3f));
        object.setLod(1);

        object.advanceLod(0.f, 0.f);

        assertEquals(-1, object.getPreviousLod());
    }

    @Test
    void testAdvanceWithoutAChangeDoesNothing() {
        final var object = object(null, level(0.3f));

        object.advanceLod(1.f, DURATION);

        assertEquals(1.f, object.getLodBlend());
        assertEquals(-1, object.getPreviousLod());
    }

    @Test
    void testThereIsNoSuchLevel() {
        final var object = object(null, level(0.3f));

        assertThrows(IllegalArgumentException.class, () -> object.setLod(2));
        assertThrows(IllegalArgumentException.class, () -> object.setLod(-1));
    }

    @Test
    void testCardsAreNotModels() {
        final var object = object(null, level(0.3f), card(0.1f));

        assertFalse(object.isFacing(0));
        assertFalse(object.isFacing(1));
        assertTrue(object.isFacing(2));

        object.setLod(2);
        assertEquals(2, object.getLod());
        assertEquals(1, object.getModelLod(), "What draws models takes the last model when the level is a card");

        object.setLod(1);
        assertEquals(1, object.getModelLod());
    }

    @Test
    void testObjectWhoseOnlySimplerLevelIsACardUsesTheMainModelAsAModel() {
        final var object = object(null, card(0.1f));
        object.setLod(1);

        assertEquals(0, object.getModelLod());
    }

    @Test
    void testASurfaceOfAnyLevelDecidesTheSortingOfTheObject() throws Exception {
        final var opaque = material("opaque");
        final var blend = material("blend");

        assertTrue(object(opaque, level(0.3f)).isSolid(), "Every level has the opaque material of the object");

        final var mixed = new LodLevel(
                new Node<>("glass", new MeshReference(
                        TestMeshes.cube(1), blend,
                        new Transform()), List.of()), 0.3f, false);
        assertFalse(object(opaque, mixed).isSolid(), "A level with glass makes the whole object one that mixes colors");
    }

    @Test
    void testCutoutIsKnownPerLevel() throws Exception {
        final var leaves = material("cutout");
        final var withHoles = new LodLevel(
                new Node<>("leaves", new MeshReference(
                        TestMeshes.cube(1), leaves,
                        new Transform()), List.of()), 0.1f, false);
        final var object = object(material("opaque"), level(0.3f), withHoles);

        assertFalse(object.hasCutout(0));
        assertFalse(object.hasCutout(1));
        assertTrue(object.hasCutout(2), "The last level has leaves");
        assertTrue(object.hasCutout(), "The object has a cutout surface in one of its levels");
        assertTrue(object.isSolid());
    }

    @Test
    void testNewMaterialOfTheObjectIsLookedAtForEveryLevel() throws Exception {
        final var object = object(material("opaque"), level(0.3f));
        assertFalse(object.hasCutout(1));

        object.setMaterial(material("cutout"));

        assertTrue(object.hasCutout(0));
        assertTrue(object.hasCutout(1));
    }
}
