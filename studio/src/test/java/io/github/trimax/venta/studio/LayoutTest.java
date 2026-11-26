package io.github.trimax.venta.studio;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The layouts of the window, read as text: what a person finds out when the studio runs and a menu is empty.
 */
final class LayoutTest {
    private static final List<String> LAYOUTS = List.of("main", "menubar", "toolbar", "hierarchy", "inspector", "bottom", "project", "console", "status");
    private static final List<String> ITEMS = List.of("MenuItem", "CheckMenuItem", "SeparatorMenuItem");

    private static Element root(final String layout) throws Exception {
        try (final var stream = LayoutTest.class.getResourceAsStream("/layouts/" + layout + ".fxml")) {
            assertNotNull(stream, layout);

            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream).getDocumentElement();
        }
    }

    @Test
    void testEveryLayoutOfTheWindowExists() throws Exception {
        for (final var layout : LAYOUTS)
            assertNotNull(root(layout), layout);
    }

    /**
     * A menu button that has a property element (a tooltip) before its items loses them: the items are not in its default property, so
     * the button has none and shows nothing. They go in an items element.
     */
    @Test
    void testItemsOfAMenuButtonAreInTheItemsElement() throws Exception {
        final var buttons = root("toolbar").getElementsByTagName("MenuButton");
        assertTrue(buttons.getLength() > 0);

        for (int index = 0; index < buttons.getLength(); index++) {
            final var button = (Element) buttons.item(index);

            for (var child = button.getFirstChild(); child != null; child = child.getNextSibling())
                if (child.getNodeType() == Node.ELEMENT_NODE)
                    assertFalse(ITEMS.contains(child.getNodeName()), "A menu button has the items loose: " + child.getNodeName());

            assertEquals(1, button.getElementsByTagName("items").getLength(), "The items element of the menu button");
        }
    }

    /** A menu that the code fills (the recent projects) has an id; the others are made in the layout and have items */
    @Test
    void testEveryMenuOfTheMenuBarHasItems() throws Exception {
        final var menus = root("menubar").getElementsByTagName("Menu");

        for (int index = 0; index < menus.getLength(); index++) {
            final var menu = (Element) menus.item(index);
            if (menu.hasAttribute("fx:id") && menu.getElementsByTagName("MenuItem").getLength() == 0)
                continue;

            assertTrue(menu.getElementsByTagName("MenuItem").getLength() + menu.getElementsByTagName("CheckMenuItem").getLength() > 0,
                    "An empty menu: " + menu.getAttribute("text"));
        }
    }
}
