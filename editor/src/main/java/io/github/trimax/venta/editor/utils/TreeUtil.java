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
import one.util.streamex.StreamEx;

@UtilityClass
public final class TreeUtil {
    public void initialize(@NonNull final TreeView<Resource> tree) {
        final var root = new TreeItem<>(Resource.asGroup(GroupType.AudioSource));
        tree.setCellFactory(_ -> new TreeCellRenderer());
        tree.setRoot(root);

        tree.getSelectionModel().selectedItemProperty()
                .addListener((_, _, newSel) -> EventUtil.post(new TreeSelectEvent(newSel)));

        enableAutoSort(root);
        sort(root);
    }

    public boolean isItemExist(@NonNull final TreeItem<Resource> item, @NonNull final String name) {
        return StreamEx.of(item.getChildren())
                .map(TreeItem::getValue)
                .filterBy(Resource::name, name)
                .findAny()
                .isPresent();
    }

    private void enableAutoSort(final TreeItem<Resource> parent) {
        for (final var child : parent.getChildren())
            enableAutoSort(child);

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
