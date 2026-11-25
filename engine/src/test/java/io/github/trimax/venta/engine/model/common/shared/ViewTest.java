package io.github.trimax.venta.engine.model.common.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.scene.SceneViewDTO;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class ViewTest {
    @Test
    void testViewWithoutSettingsIsTheDefaultOne() {
        final var view = new View(ParsingUtil.asObject("{}", SceneViewDTO.class));

        assertEquals(View.DEFAULT_NEAR, view.getNear());
        assertEquals(View.DEFAULT_FAR, view.getFar());
        assertEquals(View.DEFAULT_FOV, view.getFov());
    }

    @Test
    void testSettingsAreRead() {
        final var view = new View(ParsingUtil.asObject("{\"near\": 0.5, \"far\": 6000, \"fov\": 45}", SceneViewDTO.class));

        assertEquals(0.5f, view.getNear());
        assertEquals(6000.f, view.getFar());
        assertEquals(45.f, view.getFov());
    }

    @Test
    void testBadSettingsAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> new View(0.f, 100.f, 60.f), "A zero near plane");
        assertThrows(IllegalArgumentException.class, () -> new View(-1.f, 100.f, 60.f));
        assertThrows(IllegalArgumentException.class, () -> new View(1.f, 1.f, 60.f), "The far plane is not farther");
        assertThrows(IllegalArgumentException.class, () -> new View(5.f, 2.f, 60.f));
        assertThrows(IllegalArgumentException.class, () -> new View(0.1f, Float.POSITIVE_INFINITY, 60.f));
        assertThrows(IllegalArgumentException.class, () -> new View(0.1f, 100.f, 0.f));
        assertThrows(IllegalArgumentException.class, () -> new View(0.1f, 100.f, 180.f));
        assertThrows(IllegalArgumentException.class, () -> new View(Float.NaN, 100.f, 60.f));
    }

    @Test
    void testRefusedChangeKeepsTheOldView() {
        final var view = new View(0.5f, 2000.f, 50.f);

        assertThrows(IllegalArgumentException.class, () -> view.set(0.5f, 0.2f, 50.f));

        assertEquals(0.5f, view.getNear());
        assertEquals(2000.f, view.getFar());
        assertEquals(50.f, view.getFov());
    }

    @Test
    void testSettingFromNothingGivesTheDefaultView() {
        final var view = new View(0.5f, 2000.f, 50.f);

        view.set(null);

        assertEquals(View.DEFAULT_NEAR, view.getNear());
        assertEquals(View.DEFAULT_FAR, view.getFar());
        assertEquals(View.DEFAULT_FOV, view.getFov());
    }

    @Test
    void testWindowProjectsByItsView() {
        final var window = new WindowState(null, 0L, "test", 800, 600, 0);

        final var expected = new Matrix4f().perspective((float) Math.toRadians(60), 800.f / 600.f, 0.1f, 1000.f);
        assertTrue(expected.equals(window.getProjectionMatrix(), 1e-6f), "The default view is what the engine always had");

        window.getView().set(0.5f, 6000.f, 45.f);
        final var changed = new Matrix4f().perspective((float) Math.toRadians(45), 800.f / 600.f, 0.5f, 6000.f);
        assertTrue(changed.equals(window.getProjectionMatrix(), 1e-6f), "The view of a scene is the projection of the window");
    }
}
