package io.github.trimax.venta.editor.utils;

import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.model.event.tree.TreeSelectEvent;
import io.github.trimax.venta.editor.tree.TreeCellRenderer;
import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.model.common.resource.Resource;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import one.util.streamex.StreamEx;

@UtilityClass
public final class TreeUtil {
    /** Items that already keep their children sorted, so a listener is added only once per item */
    private final Set<TreeItem<Resource>> sortedItems = Collections.newSetFromMap(new WeakHashMap<>());

    /** Sets up the tree once: cell rendering and selection events */
    public void initialize(@NonNull final TreeView<Resource> tree) {
        tree.setCellFactory(_ -> new TreeCellRenderer());
        tree.getSelectionModel().selectedItemProperty()
                .addListener((_, _, newSel) -> EventUtil.post(new TreeSelectEvent(newSel)));

        setRoot(tree, new TreeItem<>(Resource.asGroup(GroupType.AudioSource)));
    }

    /** Shows another tree; its items are sorted and stay sorted when items are added */
    public void setRoot(@NonNull final TreeView<Resource> tree, @NonNull final TreeItem<Resource> root) {
        enableAutoSort(root);
        tree.setRoot(root);
    }

    /** Names are compared ignoring case: archive paths of such items would clash on case-insensitive file systems */
    public boolean isItemExist(@NonNull final TreeItem<Resource> item, @NonNull final String name) {
        return StreamEx.of(item.getChildren())
                .map(TreeItem::getValue)
                .anyMatch(resource -> resource.name().equalsIgnoreCase(name));
    }

    void enableAutoSort(final TreeItem<Resource> parent) {
        for (final var child : parent.getChildren())
            enableAutoSort(child);

        sort(parent);
        if (!sortedItems.add(parent))
            return;

        parent.getChildren().addListener((ListChangeListener<TreeItem<Resource>>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (final var added : change.getAddedSubList())
                        enableAutoSort(added);
                    sort(parent);
                }
            }
        });
    }

    private void sort(final TreeItem<Resource> node) {
        node.getChildren().sort((a, b) -> a.getValue().name().compareToIgnoreCase(b.getValue().name()));
    }
}
