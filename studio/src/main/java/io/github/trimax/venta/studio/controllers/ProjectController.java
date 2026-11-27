package io.github.trimax.venta.studio.controllers;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import com.google.common.eventbus.Subscribe;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.event.ProjectFileSelectedEvent;
import io.github.trimax.venta.studio.model.event.ProjectOpenedEvent;
import io.github.trimax.venta.studio.model.event.StatusEvent;
import io.github.trimax.venta.studio.services.PlacementService;
import io.github.trimax.venta.studio.services.SceneService;
import io.github.trimax.venta.studio.utils.FormatUtil;
import io.github.trimax.venta.studio.utils.IconViewUtil;
import io.github.trimax.venta.studio.utils.ProjectUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The folder of the project: the folders at the left, the files of the chosen folder at the right. A scene opens with a double click.
 */
@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjectController {
    private final StudioSession session;
    private final SceneService scenes;
    private final PlacementService placement;

    @FXML private TextField filter;
    @FXML private TreeView<Path> folders;
    @FXML private ListView<Path> files;

    private Path selectedFolder;

    @FXML
    public void initialize() {
        folders.setCellFactory(view -> new TreeCell<>() {
            @Override
            protected void updateItem(final Path folder, final boolean empty) {
                super.updateItem(folder, empty);

                setText(empty || folder == null ? null : getTreeItem().getParent() == null ? folder.getFileName().toString() + "  (project)" : folder.getFileName().toString());
                setGraphic(empty || folder == null ? null : IconViewUtil.folder(session.getProject(), folder));
            }
        });
        folders.getSelectionModel().selectedItemProperty().addListener((observable, was, is) -> {
            selectedFolder = is == null ? null : is.getValue();
            showFiles();
        });

        files.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(final Path file, final boolean empty) {
                super.updateItem(file, empty);

                setText(empty || file == null ? null : file.getFileName().toString() + "  ·  " + FormatUtil.size(file));
                setGraphic(empty || file == null ? null : IconViewUtil.file());
            }
        });
        files.getSelectionModel().selectedItemProperty().addListener((observable, was, is) -> EventUtil.post(new ProjectFileSelectedEvent(is)));
        files.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2)
                openSelected();
        });
        files.setContextMenu(createContextMenu());
        files.setOnDragDetected(event -> {
            final var file = files.getSelectionModel().getSelectedItem();
            if (file == null || !placement.isPlaceable(file))
                return;

            final var board = files.startDragAndDrop(TransferMode.COPY);
            final var content = new ClipboardContent();
            content.putFiles(List.of(file.toFile()));
            board.setContent(content);
            event.consume();
        });

        filter.textProperty().addListener((observable, was, is) -> showFiles());

        // A file that was added or changed in another program shows when the window is entered again
        session.getStage().focusedProperty().addListener((observable, was, is) -> {
            if (is && session.getProject() != null)
                refresh();
        });
    }

    @Subscribe
    public void onProjectOpened(final ProjectOpenedEvent event) {
        refresh();
    }

    @FXML
    public void onRefresh() {
        refresh();
    }

    private void refresh() {
        final var project = session.getProject();
        if (project == null)
            return;

        final var root = folderItem(project);
        root.setExpanded(true);

        final var previous = selectedFolder;
        folders.setRoot(root);
        folders.getSelectionModel().select(find(root, previous == null ? project : previous));
    }

    private TreeItem<Path> folderItem(final Path folder) {
        final var item = new TreeItem<>(folder);
        ProjectUtil.listFolders(folder).forEach(child -> item.getChildren().add(folderItem(child)));
        return item;
    }

    private TreeItem<Path> find(final TreeItem<Path> item, final Path folder) {
        if (item.getValue().equals(folder))
            return item;

        for (final var child : item.getChildren()) {
            final var found = find(child, folder);
            if (found != null)
                return found;
        }

        return item.getParent() == null ? item : null;
    }

    private void showFiles() {
        if (selectedFolder == null) {
            files.getItems().clear();
            return;
        }

        final var text = filter.getText() == null ? "" : filter.getText().trim().toLowerCase(Locale.ROOT);
        files.setItems(FXCollections.observableArrayList(ProjectUtil.listFiles(selectedFolder).stream()
                .filter(file -> text.isEmpty() || file.getFileName().toString().toLowerCase(Locale.ROOT).contains(text))
                .toList()));
    }

    private void openSelected() {
        final var file = files.getSelectionModel().getSelectedItem();
        if (file == null || session.getProject() == null)
            return;

        final var scene = ProjectUtil.sceneNameOf(session.getProject(), file);
        if (scene != null)
            scenes.open(scene);
        else
            EventUtil.post(new StatusEvent("%s is not a scene", file.getFileName()));
    }

    private ContextMenu createContextMenu() {
        final var open = new MenuItem("Open");
        open.setOnAction(event -> openSelected());

        final var reveal = new MenuItem("Show in File Manager");
        reveal.setOnAction(event -> reveal(files.getSelectionModel().getSelectedItem()));

        final var copy = new MenuItem("Copy Path");
        copy.setOnAction(event -> {
            final var file = files.getSelectionModel().getSelectedItem();
            if (file != null) {
                final var content = new ClipboardContent();
                content.putString(file.toString());
                Clipboard.getSystemClipboard().setContent(content);
            }
        });

        final var refresh = new MenuItem("Refresh");
        refresh.setOnAction(event -> refresh());

        return new ContextMenu(open, reveal, copy, refresh);
    }

    private void reveal(final Path file) {
        if (file == null || session.getHostServices() == null)
            return;

        session.getHostServices().showDocument(file.getParent().toUri().toString());
    }
}
