package io.github.trimax.venta.editor.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.model.common.resource.Resource;
import javafx.scene.control.TreeItem;

final class TreeUtilTest {
    @Test
    void testExistingNamesIgnoreCase() {
        final var group = group();
        group.getChildren().add(file("Grass.png"));

        assertTrue(TreeUtil.isItemExist(group, "Grass.png"), "Same name");
        assertTrue(TreeUtil.isItemExist(group, "grass.PNG"), "Same name in another case");
        assertFalse(TreeUtil.isItemExist(group, "Rock.png"), "Another name");
    }

    @Test
    void testTreeIsSortedAndStaysSorted() {
        final var group = group();
        group.getChildren().addAll(file("b.png"), file("C.png"));

        TreeUtil.enableAutoSort(group);
        group.getChildren().add(file("a.png"));

        assertEquals(List.of("a.png", "b.png", "C.png"), names(group), "Items must be sorted ignoring case");
    }

    @Test
    void testEnablingAutoSortTwiceIsHarmless() {
        final var group = group();
        TreeUtil.enableAutoSort(group);
        TreeUtil.enableAutoSort(group);

        group.getChildren().addAll(file("z.png"), file("m.png"));

        assertEquals(List.of("m.png", "z.png"), names(group), "Items must be sorted");
    }

    @Test
    void testAddedFoldersAreSortedToo() {
        final var group = group();
        TreeUtil.enableAutoSort(group);
        final var folder = new TreeItem<>(Resource.asFolder("folder"));
        group.getChildren().add(folder);

        folder.getChildren().addAll(file("y.png"), file("x.png"));

        assertEquals(List.of("x.png", "y.png"), names(folder), "Children of added folders must be sorted");
    }

    private static TreeItem<Resource> group() {
        return new TreeItem<>(Resource.asGroup(GroupType.Texture));
    }

    private static TreeItem<Resource> file(final String name) {
        return new TreeItem<>(Resource.asResource(name, "/" + name));
    }

    private static List<String> names(final TreeItem<Resource> item) {
        return item.getChildren().stream().map(child -> child.getValue().name()).toList();
    }
}
