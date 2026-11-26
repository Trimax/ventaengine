package io.github.trimax.venta.studio.controllers;

import java.util.Locale;

import com.google.common.eventbus.Subscribe;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.studio.model.event.OutlineSelectedEvent;
import io.github.trimax.venta.studio.model.event.SceneOpenedEvent;
import io.github.trimax.venta.studio.model.event.StatusEvent;
import io.github.trimax.venta.studio.model.outline.OutlineItem;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.model.outline.OutlineNode;
import io.github.trimax.venta.studio.utils.OutlineUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * The tree of the scene that is open: its settings, and its things by their kinds. What is chosen here is what the inspector shows.
 */
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HierarchyController {
    @FXML private TextField filter;
    @FXML private TreeView<OutlineItem> tree;

    private OutlineNode outline;

    @FXML
    public void initialize() {
        tree.setCellFactory(view -> new TreeCell<>() {
            @Override
            protected void updateItem(final OutlineItem item, final boolean empty) {
                super.updateItem(item, empty);

                setText(empty || item == null ? null : item.detail().isEmpty() ? item.name() : item.name() + "  ·  " + item.detail());
                getStyleClass().removeAll("category", "setting");
                if (!empty && item != null && item.kind() == OutlineKind.Category)
                    getStyleClass().add("category");
            }
        });

        tree.getSelectionModel().selectedItemProperty().addListener((observable, was, is) -> {
            final var item = is == null ? null : is.getValue();
            EventUtil.post(new OutlineSelectedEvent(item));

            if (item != null && item.kind() != OutlineKind.Category)
                EventUtil.post(new StatusEvent("%s `%s` selected", item.kind(), item.name()));
        });

        filter.textProperty().addListener((observable, was, is) -> rebuild());
    }

    @Subscribe
    public void onSceneOpened(final SceneOpenedEvent event) {
        outline = OutlineUtil.build(event.name(), event.scene());
        filter.clear();
        rebuild();
    }

    private void rebuild() {
        if (outline == null)
            return;

        final var item = convert(outline, filter.getText() == null ? "" : filter.getText().trim().toLowerCase(Locale.ROOT));
        tree.setRoot(item);
        tree.getSelectionModel().clearSelection();
    }

    /** The tree item of a node, null when nothing of it matches the filter: a category stays while one of its things does */
    private TreeItem<OutlineItem> convert(final OutlineNode node, final String text) {
        final var matches = text.isEmpty()
                || node.item().name().toLowerCase(Locale.ROOT).contains(text)
                || node.item().detail().toLowerCase(Locale.ROOT).contains(text);

        final var item = new TreeItem<>(node.item());
        for (final var child : node.children()) {
            final var converted = convert(child, matches && node.item().kind() == OutlineKind.Category ? "" : text);
            if (converted != null)
                item.getChildren().add(converted);
        }

        if (!matches && item.getChildren().isEmpty())
            return null;

        item.setExpanded(true);
        return item;
    }
}
