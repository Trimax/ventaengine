package io.github.trimax.venta.editor;

import io.github.trimax.venta.editor.definitions.Icons;
import io.github.trimax.venta.editor.handlers.FolderAddHandler;
import io.github.trimax.venta.editor.handlers.FolderRemoveHandler;
import io.github.trimax.venta.editor.model.Item;
import io.github.trimax.venta.editor.model.ItemType;
import io.github.trimax.venta.editor.renderers.TreeCellRenderer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

public final class ArchiveManagerController {
    @FXML private TreeView<Item> tree;
    @FXML private Label status;
    @FXML private VBox info;

    @FXML private Button btnToolBarFileAdd;
    @FXML private Button btnToolBarFileRemove;
    @FXML private Button btnToolBarFolderAdd;
    @FXML private Button btnToolBarFolderRemove;

    @FXML
    public void initialize() {
        final var root = new TreeItem<>(new Item());
        tree.setRoot(root);
        tree.getSelectionModel().selectedItemProperty().addListener((_, _, newSel) -> updateInfoPanel(newSel));
        tree.setCellFactory(_ -> new TreeCellRenderer());
        tree.setShowRoot(false);

        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, Icons.MATERIAL, "Materials", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, Icons.TEXTURE, "Textures", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, Icons.SHADER, "Shaders", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, Icons.OBJECT, "Objects", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, Icons.MESH, "Meshes", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, Icons.LIGHT, "Lights", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, Icons.SCENE, "Scenes", null)));

        btnToolBarFileAdd.setOnAction(_ -> addFile());
        btnToolBarFileRemove.setOnAction(_ -> removeFile());
        btnToolBarFolderAdd.setOnAction(new FolderAddHandler(tree, status));
        btnToolBarFolderRemove.setOnAction(new FolderRemoveHandler(tree, status));
    }

    private void addFile() {
        final var selected = tree.getSelectionModel().getSelectedItem();
        if (selected == null) {
            status.setText("Select a folder to add file");
            return;
        }

        if (selected.getValue().type() != ItemType.Folder) {
            status.setText("Selected item is not a folder");
            return;
        }

        final var newFile = new TreeItem<>(new Item(Icons.FILE, "New File","/path/to/file"));
        selected.getChildren().add(newFile);
        selected.setExpanded(true);
        status.setText("New file added");
    }

    private void removeFile() {
        final var selected = tree.getSelectionModel().getSelectedItem();
        if (selected == null) {
            status.setText("Select a file or a folder to remove");
            return;
        }

        final var item = selected.getValue();
        if (item.type() == ItemType.Root || item.type() == ItemType.Group) {
            status.setText("Neither root nor group can be removed");
            return;
        }

        selected.getParent().getChildren().remove(selected);
        status.setText("File removed");
    }

    private void updateInfoPanel(final TreeItem<Item> selected) {
        info.getChildren().clear();
        if (selected != null) {
            final var nameLabel = new Label("Name: " + selected.getValue().name());
            final var typeLabel = new Label(selected.getChildren().isEmpty() ? "Type: File" : "Type: Folder");
            info.getChildren().addAll(nameLabel, typeLabel);
        }
    }
}
