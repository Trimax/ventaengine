package io.github.trimax.venta.editor;

import io.github.trimax.venta.editor.model.Item;
import io.github.trimax.venta.editor.model.ItemType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public final class ArchiveManagerController {
    @FXML private TreeView<Item> resourceTree;
    @FXML private VBox infoPanel;
    @FXML private Label statusLabel;

    @FXML private Button btnToolBarFileAdd;
    @FXML private Button btnToolBarFileRemove;
    @FXML private Button btnToolBarFolderAdd;
    @FXML private Button btnToolBarFolderRemove;

    @FXML
    public void initialize() {
        final var root = new TreeItem<>(new Item(ItemType.Root, "Root", null));
        resourceTree.setRoot(root);
        resourceTree.getSelectionModel().selectedItemProperty().addListener((_, _, newSel) -> updateInfoPanel(newSel));
        resourceTree.setShowRoot(false);
        resourceTree.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(final Item item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setText(null);
                else
                    setText(item.name());
            }
        });

        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, "Shaders", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, "Textures", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, "Objects", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, "Meshes", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, "Scenes", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, "Materials", null)));

        btnToolBarFileAdd.setOnAction(_ -> addFile());
        btnToolBarFileRemove.setOnAction(_ -> removeFile());
        btnToolBarFolderAdd.setOnAction(_ -> addFolder());
        btnToolBarFolderRemove.setOnAction(_ -> removeFolder());
    }

    private void addFile() {
        final var selected = resourceTree.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a folder to add file");
            return;
        }

        if (selected.getValue().type() != ItemType.Folder) {
            statusLabel.setText("Selected item is not a folder");
            return;
        }

        final var newFile = new TreeItem<>(new Item("New File", "/path/to/file"));
        selected.getChildren().add(newFile);
        selected.setExpanded(true);
        statusLabel.setText("New file added");
    }

    private void removeFile() {
        final var selected = resourceTree.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a file or a folder to remove");
            return;
        }

        final var item = selected.getValue();
        if (item.type() == ItemType.Root || item.type() == ItemType.Group) {
            statusLabel.setText("Neither root nor group can be removed");
            return;
        }

        selected.getParent().getChildren().remove(selected);
        statusLabel.setText("File removed");
    }

    private void addFolder() {
        final var selected = resourceTree.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a group or a folder to add folder");
            return;
        }

        final var item = selected.getValue();
        if (item.type() == ItemType.Root || item.type() == ItemType.File) {
            statusLabel.setText("Neither root nor file can be used for adding folders");
            return;
        }

        final var newFolder = new TreeItem<>(new Item("New Folder"));
        selected.getChildren().add(newFolder);
        selected.setExpanded(true);

        statusLabel.setText("New folder added");
    }

    private void removeFolder() {
        final var selected = resourceTree.getSelectionModel().getSelectedItem();

        if (selected != null && selected != resourceTree.getRoot() && !selected.getChildren().isEmpty()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete folder and all its contents?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    selected.getParent().getChildren().remove(selected);
                    statusLabel.setText("Folder removed");
                }
            });
        } else if (selected != null && selected != resourceTree.getRoot()) {
            // Empty folder
            selected.getParent().getChildren().remove(selected);
            statusLabel.setText("Folder removed");
        } else {
            statusLabel.setText("Select a folder to remove");
        }
    }

    private void updateInfoPanel(TreeItem<Item> selected) {
        infoPanel.getChildren().clear();
        if (selected != null) {
            Label nameLabel = new Label("Name: " + selected.getValue().name());
            Label typeLabel = new Label(selected.getChildren().isEmpty() ? "Type: File" : "Type: Folder");
            infoPanel.getChildren().addAll(nameLabel, typeLabel);
        }
    }
}
