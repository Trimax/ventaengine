package io.github.trimax.venta.studio.controllers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import com.google.common.eventbus.Subscribe;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.studio.controls.EntryEditor;
import io.github.trimax.venta.studio.model.event.OutlineSelectedEvent;
import io.github.trimax.venta.studio.model.event.SceneChangedEvent;
import io.github.trimax.venta.studio.services.SceneEditService;
import io.github.trimax.venta.studio.model.event.ProjectFileSelectedEvent;
import io.github.trimax.venta.studio.model.outline.OutlineItem;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.model.property.Property;
import io.github.trimax.venta.studio.utils.FormatUtil;
import io.github.trimax.venta.studio.utils.ProjectUtil;
import io.github.trimax.venta.studio.utils.PropertyUtil;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.beans.property.ReadOnlyStringWrapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Shows what is chosen: the fields of an item of the hierarchy, or the contents of a file of the project. Nothing is changed here yet.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class InspectorController {
    private final SceneEditService edits;

    private static final int TEXT_LIMIT = 64 * 1024;

    @FXML private Label heading;
    @FXML private Label subheading;
    @FXML private StackPane content;

    private EntryEditor editor;

    @FXML
    public void initialize() {
        clear();
    }

    @Subscribe
    public void onOutlineSelected(final OutlineSelectedEvent event) {
        final var item = event.item();
        editor = null;

        if (item == null || item.kind() == OutlineKind.Category) {
            clear();
            return;
        }

        if (item.isThing() && edits.getDocument() != null) {
            editor = new EntryEditor(edits, item.kind(), item.index(), item.detail());
            show(item.kind().name(), "", editor);
            return;
        }

        show(item.name(), item.kind() + (item.detail().isEmpty() ? "" : " · " + item.detail()), createProperties(item));
    }

    /** The scene was changed (an edit, an undo): what is shown is what the file says */
    @Subscribe
    public void onSceneChanged(final SceneChangedEvent event) {
        if (editor != null)
            editor.refresh();
    }

    @Subscribe
    public void onFileSelected(final ProjectFileSelectedEvent event) {
        editor = null;

        final var file = event.file();
        if (file == null) {
            clear();
            return;
        }

        switch (ProjectUtil.previewOf(file)) {
            case Image -> showImage(file);
            case Text -> showText(file);
            default -> show(file.getFileName().toString(), "Binary file · " + FormatUtil.size(file), new Label("There is nothing to show for this file yet"));
        }
    }

    private void clear() {
        show("Nothing is selected", "Choose something in the hierarchy or a file of the project", null);
    }

    private void show(final String title, final String subtitle, final Node node) {
        heading.setText(title);
        subheading.setText(subtitle);
        content.getChildren().setAll(node == null ? new Label() : node);
    }

    private Node createProperties(final OutlineItem item) {
        final var root = new TreeItem<>(new Property("", ""));
        PropertyUtil.read(item.source()).forEach(property -> root.getChildren().add(convert(property)));
        root.setExpanded(true);

        final var table = new TreeTableView<>(root);
        table.setShowRoot(false);
        table.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        final var names = new TreeTableColumn<Property, String>("Property");
        names.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getValue().name()));
        names.setPrefWidth(130);

        final var values = new TreeTableColumn<Property, String>("Value");
        values.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getValue().value()));

        table.getColumns().add(names);
        table.getColumns().add(values);
        return table;
    }

    private TreeItem<Property> convert(final Property property) {
        final var item = new TreeItem<>(property);
        property.children().forEach(child -> item.getChildren().add(convert(child)));
        item.setExpanded(true);
        return item;
    }

    private void showImage(final Path file) {
        final var image = new Image(file.toUri().toString());
        final var view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setFitWidth(Math.min(image.getWidth() > 0 ? image.getWidth() : 256, 512));

        final var scroll = new ScrollPane(view);
        scroll.setPannable(true);
        show(file.getFileName().toString(), String.format(Locale.ROOT, "Image · %d x %d · %s", (int) image.getWidth(), (int) image.getHeight(), FormatUtil.size(file)), scroll);
    }

    private void showText(final Path file) {
        final var area = new TextArea();
        area.setEditable(false);
        area.setStyle("-fx-font-family: monospace;");

        try {
            final var bytes = Files.readAllBytes(file);
            final var text = new String(bytes, 0, Math.min(bytes.length, TEXT_LIMIT), StandardCharsets.UTF_8);
            area.setText(bytes.length > TEXT_LIMIT ? text + "\n… (the rest of the file is not shown)" : text);
        } catch (final IOException exception) {
            area.setText("The file can't be read: " + exception.getMessage());
        }

        show(file.getFileName().toString(), "Text · " + FormatUtil.size(file), area);
    }
}
