package io.github.trimax.venta.studio.model.document;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.enums.ToneMapping;

final class SceneDocumentSectionTest {
    private static final String SCENE = """
            {
              "ambientLight": {"r": 0.3, "g": 0.3, "b": 0.3, "a": 1.0},
              "customThing": {"anything": [1, 2, 3]},
              "fog": {"density": 0.001, "color": {"r": 1.0, "g": 0.5, "b": 0.2, "a": 1.0}, "somethingNew": 7},
              "skybox": "sunset.json",
              "exposure": 0.6,
              "toneMapping": "Neutral"
            }
            """;

    private final SceneDocument document = new SceneDocument("s.json", SCENE);

    @Test
    void testSectionsOfTheFileAreFound() {
        assertTrue(document.hasSection("Fog"));
        assertTrue(document.hasSection("Ambient light"));
        assertTrue(document.hasSection("Output"), "The exposure and the tone mapping are the output");
        assertFalse(document.hasSection("Bloom"), "The file has no bloom");
        assertFalse(document.hasSection("Nonsense"));
    }

    @Test
    void testSectionIsACopy() {
        final var fog = document.getSection("Fog");
        fog.addProperty("density", 5.0);

        assertEquals(0.001f, document.getScene().fog().density(), 1e-6f, "The document is changed by setSection only");
    }

    @Test
    void testChangedSectionIsInTheSceneAndTheRestIsAsItWas() {
        final var fog = document.getSection("Fog");
        fog.addProperty("density", 0.5);
        document.setSection("Fog", fog);

        assertEquals(0.5f, document.getScene().fog().density(), 1e-6f);
        assertTrue(document.toJson().contains("\"somethingNew\": 7"), "A field that the studio doesn't know is kept");
        assertTrue(document.toJson().contains("\"customThing\""));
        assertTrue(document.toJson().indexOf("\"fog\"") < document.toJson().indexOf("\"exposure\""), "The order of the fields is kept");
    }

    @Test
    void testColorIsASection() {
        final var color = document.getSection("Ambient light");
        color.addProperty("r", 0.9);
        document.setSection("Ambient light", color);

        assertEquals(0.9f, document.getScene().ambientLight().r(), 1e-6f);
    }

    @Test
    void testOutputIsMadeOfTwoFieldsOfTheFile() {
        final var output = document.getSection("Output");
        assertEquals(0.6f, output.get("exposure").getAsFloat(), 1e-6f);
        assertEquals("Neutral", output.get("toneMapping").getAsString());

        output.addProperty("exposure", 1.5);
        output.addProperty("toneMapping", "ACES");
        document.setSection("Output", output);

        assertEquals(1.5f, document.getScene().exposure(), 1e-6f);
        assertEquals(ToneMapping.ACES, document.getScene().toneMapping());
    }

    @Test
    void testFieldOfTheOutputThatTheSectionLacksIsTakenOutOfTheFile() {
        final var output = document.getSection("Output");
        output.remove("toneMapping");
        document.setSection("Output", output);

        assertNull(document.getScene().toneMapping());
        assertFalse(document.toJson().contains("toneMapping"));
    }

    @Test
    void testSkyboxIsAnObjectWithTheNameOfTheCubemap() {
        final var skybox = document.getSection("Skybox");
        assertEquals("sunset.json", skybox.get("cubemap").getAsString());

        skybox.addProperty("cubemap", "stars.json");
        document.setSection("Skybox", skybox);

        assertEquals("stars.json", document.getScene().skybox());
        assertTrue(document.toJson().contains("\"skybox\": \"stars.json\""), "It is a name in the file, not an object");
    }

    @Test
    void testNameThatIsNotASectionIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> document.getSection("Bloom"));
        assertThrows(IllegalArgumentException.class, () -> document.setSection("Nonsense", new com.google.gson.JsonObject()));
    }
}
