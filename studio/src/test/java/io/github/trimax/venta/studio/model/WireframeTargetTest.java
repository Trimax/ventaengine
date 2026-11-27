package io.github.trimax.venta.studio.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * The check items of the menus name the kinds by the user data: a name that is not a kind would fail when the item is clicked.
 */
final class WireframeTargetTest {
    private static final Pattern USER_DATA = Pattern.compile("<CheckMenuItem[^>]*userData=\"([A-Za-z]+)\"");

    private static Set<WireframeTarget> targetsOf(final String layout) throws IOException {
        try (final var stream = WireframeTargetTest.class.getResourceAsStream(layout)) {
            assertNotNull(stream, layout);

            final var targets = EnumSet.noneOf(WireframeTarget.class);
            final var matcher = USER_DATA.matcher(new String(stream.readAllBytes()));
            while (matcher.find())
                targets.add(WireframeTarget.valueOf(matcher.group(1)));

            return targets;
        }
    }

    @Test
    void testToolbarHasEveryKind() throws IOException {
        assertEquals(EnumSet.allOf(WireframeTarget.class), targetsOf("/layouts/toolbar.fxml"));
    }

    @Test
    void testMenuHasEveryKind() throws IOException {
        assertEquals(EnumSet.allOf(WireframeTarget.class), targetsOf("/layouts/menubar.fxml"));
    }
}
