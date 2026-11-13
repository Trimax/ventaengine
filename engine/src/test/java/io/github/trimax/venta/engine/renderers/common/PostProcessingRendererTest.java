package io.github.trimax.venta.engine.renderers.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

final class PostProcessingRendererTest {
    @Test
    void testSampleCountsAreHalvedDownToNoMultisampling() {
        assertEquals(List.of(8, 4, 2, 1, 0), PostProcessingRenderer.getSampleCounts(8), "8 samples");
        assertEquals(List.of(4, 2, 1, 0), PostProcessingRenderer.getSampleCounts(4), "4 samples");
    }

    @Test
    void testWithoutMultisamplingOnlyZeroIsTried() {
        assertEquals(List.of(0), PostProcessingRenderer.getSampleCounts(0), "No multisampling");
        assertEquals(List.of(0), PostProcessingRenderer.getSampleCounts(-1), "A negative count is no multisampling");
    }

    @Test
    void testOddCountsAreHalvedDown() {
        assertEquals(List.of(6, 3, 1, 0), PostProcessingRenderer.getSampleCounts(6), "6 samples");
    }
}
