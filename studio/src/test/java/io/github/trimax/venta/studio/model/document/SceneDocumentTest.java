package io.github.trimax.venta.studio.model.document;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonParser;

import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.utils.EntryUtil;

final class SceneDocumentTest {
    private static final String SCENE = """
            {
              "ambientLight": {"r": 0.3, "g": 0.3, "b": 0.3, "a": 1.0},
              "customThing": {"anything": [1, 2, 3]},
              "objects": [
                {"name": "Chest", "object": "chest.json", "position": {"x": 1, "y": 2, "z": 3}, "angles": {"x": 0, "y": 0.35, "z": 0}, "scale": {"x": 1, "y": 1, "z": 1}},
                {"name": "Palm", "object": "palm.json", "position": {"x": -4, "y": 0, "z": 8}, "angles": {"x": 0, "y": 0, "z": 0}, "scale": {"x": 6, "y": 6, "z": 6}}
              ],
              "exposure": 0.6
            }
            """;

    @Test
    void testDocumentShowsTheSceneItIsMadeOf() {
        final var document = new SceneDocument("s.json", SCENE);

        assertEquals("s.json", document.getName());
        assertEquals(2, document.getScene().objects().size());
        assertEquals("Palm", document.getScene().objects().get(1).name());
        assertEquals(2, document.count(OutlineKind.Object));
        assertEquals(0, document.count(OutlineKind.Light), "The file has no lights");
    }

    @Test
    void testEntryIsACopy() {
        final var document = new SceneDocument("s.json", SCENE);

        final var entry = document.getEntry(OutlineKind.Object, 0);
        EntryUtil.setName(entry, "Changed");

        assertEquals("Chest", document.getScene().objects().get(0).name(), "The document is changed by setEntry only");
    }

    @Test
    void testChangedEntryIsInTheSceneAndNothingElseIsTouched() {
        final var document = new SceneDocument("s.json", SCENE);
        final var entry = document.getEntry(OutlineKind.Object, 1);
        EntryUtil.setName(entry, "Tree");

        document.setEntry(OutlineKind.Object, 1, entry);

        assertEquals("Tree", document.getScene().objects().get(1).name());
        assertEquals("Chest", document.getScene().objects().get(0).name());
        assertEquals(0.6f, document.getScene().exposure());
    }

    @Test
    void testWrittenFileKeepsTheOrderOfTheFieldsAndWhatTheEngineDoesNotKnow() {
        final var document = new SceneDocument("s.json", SCENE);
        final var entry = document.getEntry(OutlineKind.Object, 0);
        EntryUtil.setName(entry, "Box");
        document.setEntry(OutlineKind.Object, 0, entry);

        final var written = JsonParser.parseString(document.toJson()).getAsJsonObject();

        assertEquals(List.of("ambientLight", "customThing", "objects", "exposure"), List.copyOf(written.keySet()), "The order of the file");
        assertEquals("[1,2,3]", written.getAsJsonObject("customThing").get("anything").toString(), "The field that the studio doesn't know is kept");
        assertEquals("Box", written.getAsJsonArray("objects").get(0).getAsJsonObject().get("name").getAsString());
    }

    @Test
    void testNumbersThatWereNotTouchedAreWrittenAsTheyWere() {
        final var document = new SceneDocument("s.json", "{\"exposure\": 0.60, \"objects\": [{\"name\": \"A\", \"object\": \"a.json\", \"position\": {\"x\": 300, \"y\": 1.50, \"z\": 0}, \"angles\": {\"x\": 0, \"y\": 0, \"z\": 0}, \"scale\": {\"x\": 1, \"y\": 1, \"z\": 1}}]}");

        assertTrue(document.toJson().contains("0.60"), "The text of the number is kept");
        assertTrue(document.toJson().contains("300"));
    }

    @Test
    void testWrittenFileIsIndentedAndEndsWithALine() {
        final var text = new SceneDocument("s.json", SCENE).toJson();

        assertTrue(text.contains("\n  \"objects\": ["), "Two spaces");
        assertTrue(text.endsWith("}\n"));
    }

    @Test
    void testThingCanBeInsertedInThePlaceAndTheOthersMoveOn() {
        final var document = new SceneDocument("s.json", SCENE);
        final var entry = document.getEntry(OutlineKind.Object, 0);
        EntryUtil.setName(entry, "Inserted");

        document.insertEntry(OutlineKind.Object, 1, entry);

        assertEquals(List.of("Chest", "Inserted", "Palm"), document.getScene().objects().stream().map(object -> object.name()).toList());
    }

    @Test
    void testThingCanBeInsertedAtTheEndAndInAnEmptyKind() {
        final var document = new SceneDocument("s.json", SCENE);
        final var entry = document.getEntry(OutlineKind.Object, 0);
        final var light = JsonParser.parseString("{\"name\": \"Lamp\", \"light\": \"lamp.json\", \"position\": {\"x\": 0, \"y\": 4, \"z\": 0}}").getAsJsonObject();

        document.insertEntry(OutlineKind.Object, 2, entry);
        document.insertEntry(OutlineKind.Light, 0, light);

        assertEquals(3, document.count(OutlineKind.Object));
        assertEquals(1, document.count(OutlineKind.Light), "The array is made");
    }

    @Test
    void testThingCanBeTakenOut() {
        final var document = new SceneDocument("s.json", SCENE);

        document.removeEntry(OutlineKind.Object, 0);

        assertEquals(List.of("Palm"), document.getScene().objects().stream().map(object -> object.name()).toList());
    }

    @Test
    void testPlacesOutsideOfTheFileAreRefused() {
        final var document = new SceneDocument("s.json", SCENE);

        assertThrows(IndexOutOfBoundsException.class, () -> document.getEntry(OutlineKind.Object, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> document.getEntry(OutlineKind.Object, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> document.removeEntry(OutlineKind.Light, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> document.insertEntry(OutlineKind.Object, 3, document.getEntry(OutlineKind.Object, 0)));
        assertThrows(IllegalArgumentException.class, () -> document.getEntry(OutlineKind.Setting, 0), "A setting is not a thing of the file");
    }

    @Test
    void testFileThatIsNotASceneIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new SceneDocument("s.json", "[1, 2]"));
        assertThrows(RuntimeException.class, () -> new SceneDocument("s.json", "{ not json"));
    }

    @Test
    void testKindsThatAreThingsAreTheOnesOfTheFile() {
        assertTrue(SceneDocument.isThingKind(OutlineKind.Object));
        assertTrue(SceneDocument.isThingKind(OutlineKind.Water));
        assertFalse(SceneDocument.isThingKind(OutlineKind.Setting));
        assertFalse(SceneDocument.isThingKind(OutlineKind.Category));
    }

    /** What the studio writes must be what the engine reads: every scene of the examples is read, written and read again */
    @Test
    void testEveryExampleSceneSurvivesBeingWritten() throws IOException {
        final var folder = Path.of("..", "examples", "src", "main", "resources", "scenes");
        if (!Files.isDirectory(folder))
            return;

        try (final Stream<Path> files = Files.list(folder)) {
            for (final var file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                final var text = Files.readString(file);
                final var document = new SceneDocument(file.getFileName().toString(), text);
                final var again = new SceneDocument(file.getFileName().toString(), document.toJson());

                assertEquals(JsonParser.parseString(text), JsonParser.parseString(document.toJson()), file + " is the same tree");
                assertEquals(document.getScene(), again.getScene(), file + " is the same scene");
            }
        }
    }
}
