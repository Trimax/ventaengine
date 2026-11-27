package io.github.trimax.venta.studio.model.document;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

final class ResourceDocumentTest {
    private static final String LIGHT = """
            {
              "type": "Spot",
              "intensity": 1.5,
              "range": 100.0,
              "somethingNew": [1, 2]
            }
            """;

    private final ResourceDocument document = new ResourceDocument("lights/spot.json", LIGHT);

    @Test
    void testDocumentKnowsWhereItIs() {
        assertEquals("lights/spot.json", document.getPath());
        assertFalse(document.isModified());
    }

    @Test
    void testContentsAreACopy() {
        final var contents = document.get();
        contents.addProperty("range", 5.0);

        assertEquals(100.0, document.get().get("range").getAsDouble(), "The document is changed by set only");
        assertFalse(document.isModified());
    }

    @Test
    void testChangedDocumentIsModifiedUntilItIsSaved() {
        final var contents = document.get();
        contents.addProperty("range", 5.0);
        document.set(contents);
        assertTrue(document.isModified());

        document.markSaved();
        assertFalse(document.isModified());
    }

    @Test
    void testChangeThatIsTakenBackIsNotAModification() {
        final var original = document.get();
        final var changed = document.get();
        changed.addProperty("range", 5.0);

        document.set(changed);
        document.set(original);

        assertFalse(document.isModified(), "Nothing is written for a file that is as it was");
    }

    @Test
    void testWrittenTextKeepsTheFieldsThatTheEditorDoesNotKnowAndTheirOrder() {
        final var contents = document.get();
        contents.addProperty("range", 7.0);
        document.set(contents);

        final var text = document.toJson();
        assertTrue(text.contains("\"somethingNew\""));
        assertTrue(text.indexOf("\"type\"") < text.indexOf("\"intensity\"") && text.indexOf("\"intensity\"") < text.indexOf("\"range\""));
        assertTrue(text.endsWith("}\n"));
    }

    @Test
    void testFileThatIsNotAnObjectIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new ResourceDocument("lights/x.json", "[1, 2]"));
    }
}
