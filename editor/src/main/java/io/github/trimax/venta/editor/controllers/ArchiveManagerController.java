package io.github.trimax.venta.editor.controllers;

import io.github.trimax.venta.editor.definitions.Icons;
import io.github.trimax.venta.editor.handlers.FileAddHandler;
import io.github.trimax.venta.editor.handlers.FileRemoveHandler;
import io.github.trimax.venta.editor.handlers.FolderAddHandler;
import io.github.trimax.venta.editor.handlers.FolderRemoveHandler;
import io.github.trimax.venta.editor.model.Item;
import io.github.trimax.venta.editor.model.ItemType;
import io.github.trimax.venta.editor.renderers.TreeCellRenderer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public final class ArchiveManagerController {
    @FXML private TreeView<Item> tree;
    @FXML private Label status;
    @FXML private VBox info;

    @FXML private Button btnToolBarFileAdd;
    @FXML private Button btnToolBarFileRemove;
    @FXML private Button btnToolBarFolderAdd;
    @FXML private Button btnToolBarFolderRemove;

    @FXML private MenuItem btnMenuFileAdd;
    @FXML private MenuItem btnMenuFileRemove;
    @FXML private MenuItem btnMenuFolderAdd;
    @FXML private MenuItem btnMenuFolderRemove;

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

        btnToolBarFileAdd.setOnAction(new FileAddHandler(tree, status));
        btnToolBarFileRemove.setOnAction(new FileRemoveHandler(tree, status));
        btnToolBarFolderAdd.setOnAction(new FolderAddHandler(tree, status));
        btnToolBarFolderRemove.setOnAction(new FolderRemoveHandler(tree, status));

        btnMenuFileAdd.setOnAction(new FileAddHandler(tree, status));
        btnMenuFileRemove.setOnAction(new FileRemoveHandler(tree, status));
        btnMenuFolderAdd.setOnAction(new FolderAddHandler(tree, status));
        btnMenuFolderRemove.setOnAction(new FolderRemoveHandler(tree, status));
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
