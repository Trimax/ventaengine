package io.github.trimax.venta.studio.controls;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

final class SectionEditorTest {
    @Test
    void testStepIsAFractionOfTheOrderOfTheNumber() {
        assertEquals(10.f, SectionEditor.stepOf(300.0));
        assertEquals(1.f, SectionEditor.stepOf(40.0));
        assertEquals(0.1f, SectionEditor.stepOf(2.5));
        assertEquals(0.01f, SectionEditor.stepOf(0.18));
        assertEquals(0.001f, SectionEditor.stepOf(0.0011), "A small number such as the density of the fog is not jumped over");
        assertEquals(0.001f, SectionEditor.stepOf(0.0));
        assertEquals(0.1f, SectionEditor.stepOf(-2.5), "A negative number is the same");
    }
}
