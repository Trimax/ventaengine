package io.github.trimax.venta.engine.model.common.reflection;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.memory.TestMemory;
import io.github.trimax.venta.engine.model.dto.MaterialReflectionsDTO;
import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.TestObjects;
import io.github.trimax.venta.engine.shaders.GLTestContext;

/**
 * The cube map of a probe: the faces are drawn one by one, and the probe is ready when all of them are.
 */
final class ReflectionProbeTest {
    private static Memory memory;
    private static ObjectInstanceImplementation object;

    @BeforeAll
    static void context() throws Exception {
        GLTestContext.require();

        memory = TestMemory.create();
        object = TestObjects.object(null);
    }

    @AfterAll
    static void cleanup() {
        if (memory != null)
            memory.cleanup();
    }

    private static ReflectionProbe probe(final int resolution, final float refresh) {
        return new ReflectionProbe(memory, object, new ReflectionSettings(new MaterialReflectionsDTO(resolution, refresh, 40.f)));
    }

    private static void drawAllFaces(final ReflectionProbe probe, final long frame) {
        while (probe.isDrawing()) {
            probe.bindFace(probe.getNextFace());
            probe.finishFace(frame);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Test
    void testProbeIsNotReadyUntilAllFacesAreDrawn() {
        final var probe = probe(16, 0.f);
        try {
            assertFalse(probe.isReady());
            assertFalse(probe.isDrawing());

            probe.advance(0.01f);
            assertTrue(probe.isDrawing(), "A probe that was never drawn starts at once");
            assertEquals(6, probe.getFacesLeft());

            for (int face = 0; face < 5; face++) {
                assertEquals(face, probe.getNextFace());
                probe.bindFace(face);
                probe.finishFace(face);
                assertFalse(probe.isReady(), "The face " + face + " is not the last one");
            }

            probe.bindFace(probe.getNextFace());
            probe.finishFace(5);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);

            assertTrue(probe.isReady());
            assertFalse(probe.isDrawing());
        } finally {
            probe.release();
        }
    }

    @Test
    void testStaticProbeIsDrawnOnce() {
        final var probe = probe(16, 0.f);
        try {
            probe.advance(0.f);
            drawAllFaces(probe, 1);

            probe.advance(1000.f);
            assertFalse(probe.isDrawing());
            assertTrue(probe.isReady());
        } finally {
            probe.release();
        }
    }

    @Test
    void testProbeIsDrawnAgainWhenItsRefreshComes() {
        final var probe = probe(16, 0.5f);
        try {
            probe.advance(0.f);
            drawAllFaces(probe, 1);

            probe.advance(0.2f);
            assertFalse(probe.isDrawing(), "The refresh is not due");

            probe.advance(0.4f);
            assertTrue(probe.isDrawing());
            assertTrue(probe.isReady(), "The old cube map is used while the new faces are drawn");
        } finally {
            probe.release();
        }
    }

    @Test
    void testLevelsAreFromTheFaceToOnePixel() {
        final var probe = probe(128, 0.f);
        try {
            assertEquals(8, probe.getLevels());

            glBindTexture(GL_TEXTURE_CUBE_MAP, probe.getTextureID());
            assertEquals(1, glGetTexLevelParameteri(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 7, GL_TEXTURE_WIDTH));
            assertEquals(128, glGetTexLevelParameteri(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL_TEXTURE_WIDTH));
            glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        } finally {
            probe.release();
        }
    }

    @Test
    void testFaceIsDrawnIntoItsOwnImageWithTheMipLevels() {
        final var probe = probe(16, 0.f);
        try {
            glClearColor(0.5f, 0.25f, 2.f, 1.f);
            probe.advance(0.f);
            probe.bindFace(2);
            probe.finishFace(1);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);

            final var data = MemoryUtil.memAllocFloat(16 * 16 * 4);
            final var small = MemoryUtil.memAllocFloat(4 * 4 * 4);
            try {
                glBindTexture(GL_TEXTURE_CUBE_MAP, probe.getTextureID());
                glGetTexImage(GL_TEXTURE_CUBE_MAP_POSITIVE_X + 2, 0, GL_RGBA, GL_FLOAT, data);
                glGetTexImage(GL_TEXTURE_CUBE_MAP_POSITIVE_X + 2, 2, GL_RGBA, GL_FLOAT, small);

                final var index = (8 * 16 + 8) * 4;
                assertEquals(0.5f, data.get(index), 0.01f);
                assertEquals(0.25f, data.get(index + 1), 0.01f);
                assertEquals(2.f, data.get(index + 2), 0.02f, "HDR colors are kept above 1");
                assertEquals(2.f, small.get(5 * 4 + 2), 0.02f, "The smaller level is made of the face");
            } finally {
                glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
                MemoryUtil.memFree(data);
                MemoryUtil.memFree(small);
                glClearColor(0.f, 0.f, 0.f, 0.f);
            }
        } finally {
            probe.release();
        }
    }
}
