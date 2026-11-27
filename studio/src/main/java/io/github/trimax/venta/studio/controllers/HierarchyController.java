package io.github.trimax.venta.studio.controllers;

import java.util.Locale;

import com.google.common.eventbus.Subscribe;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.studio.model.event.OutlineSelectedEvent;
import io.github.trimax.venta.studio.model.event.OutlineSelectionRequestEvent;
import io.github.trimax.venta.studio.model.event.SceneChangedEvent;
import io.github.trimax.venta.studio.model.event.SceneOpenedEvent;
import io.github.trimax.venta.studio.model.event.StatusEvent;
import io.github.trimax.venta.studio.model.outline.OutlineItem;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.model.outline.OutlineNode;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import io.github.trimax.venta.studio.services.PlacementService;
import io.github.trimax.venta.studio.services.SelectionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * The tree of the scene that is open: its settings, and its things by their kinds. What is chosen here is what the inspector shows.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HierarchyController {
    private final SelectionService selection;
    private final PlacementService placement;

    @FXML private TextField filter;
    @FXML private TreeView<OutlineItem> tree;

    private OutlineNode outline;

    /** Whether the tree is made again: what it chooses then is not a choice of the user */
    private boolean rebuilding;

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
            if (rebuilding)
                return;

            final var item = is == null ? null : is.getValue();
            EventUtil.post(new OutlineSelectedEvent(item));

            if (item != null && item.kind() != OutlineKind.Category)
                EventUtil.post(new StatusEvent("%s `%s` selected", item.kind(), item.name()));
        });

        filter.textProperty().addListener((observable, was, is) -> rebuild());
        tree.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles() && !event.getDragboard().getFiles().isEmpty() && placement.isPlaceable(event.getDragboard().getFiles().getFirst().toPath()))
                event.acceptTransferModes(TransferMode.COPY);

            event.consume();
        });
        tree.setOnDragDropped(event -> {
            final var accepted = event.getDragboard().hasFiles() && !event.getDragboard().getFiles().isEmpty()
                    && placement.place(event.getDragboard().getFiles().getFirst().toPath(), null, null);

            event.setDropCompleted(accepted);
            event.consume();
        });
        tree.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2)
                selection.focusSelection();
        });
    }

    /** The viewport chose a thing: its item is chosen here too, and shown */
    @Subscribe
    public void onSelectionRequested(final OutlineSelectionRequestEvent event) {
        final var found = event.item() == null || tree.getRoot() == null ? null : find(tree.getRoot(), event.item());
        if (found == null) {
            tree.getSelectionModel().clearSelection();
            return;
        }

        tree.getSelectionModel().select(found);
        tree.scrollTo(tree.getRow(found));
    }

    /** The scene was changed: the tree shows it, and the thing that was chosen is chosen again (by its place in the file: its name may be new) */
    @Subscribe
    public void onSceneChanged(final SceneChangedEvent event) {
        final var chosen = tree.getSelectionModel().getSelectedItem() == null ? null : tree.getSelectionModel().getSelectedItem().getValue();
        outline = event.outline();

        rebuilding = true;
        try {
            rebuild();

            final var found = chosen == null ? null : findAgain(tree.getRoot(), chosen);
            if (found != null)
                tree.getSelectionModel().select(found);
        } finally {
            rebuilding = false;
        }
    }

    private TreeItem<OutlineItem> findAgain(final TreeItem<OutlineItem> node, final OutlineItem chosen) {
        if (node == null)
            return null;

        final var value = node.getValue();
        if (value.kind() == chosen.kind() && (chosen.isThing() ? value.index() == chosen.index() : value.name().equals(chosen.name())))
            return node;

        for (final var child : node.getChildren()) {
            final var found = findAgain(child, chosen);
            if (found != null)
                return found;
        }

        return null;
    }

    @Subscribe
    public void onSceneOpened(final SceneOpenedEvent event) {
        outline = event.outline();
        filter.clear();
        rebuild();
    }

    private TreeItem<OutlineItem> find(final TreeItem<OutlineItem> node, final OutlineItem item) {
        if (node.getValue() == item)
            return node;

        for (final var child : node.getChildren()) {
            final var found = find(child, item);
            if (found != null)
                return found;
        }

        return null;
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
