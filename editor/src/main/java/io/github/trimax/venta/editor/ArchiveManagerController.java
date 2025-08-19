package io.github.trimax.venta.editor;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public final class ArchiveManagerController {
    @FXML private TreeView<String> resourceTree;
    @FXML private VBox infoPanel;
    @FXML private Label statusLabel;

    @FXML private Button btnToolBarFileAdd;
    @FXML private Button btnToolBarFileRemove;
    @FXML private Button btnToolBarFolderAdd;
    @FXML private Button btnToolBarFolderRemove;

    @FXML
    public void initialize() {
        resourceTree.setRoot(new TreeItem<>("Root"));
        resourceTree.getSelectionModel().selectedItemProperty().addListener((_, _, newSel) -> updateInfoPanel(newSel));

        btnToolBarFileAdd.setOnAction(_ -> addFile());
        btnToolBarFileRemove.setOnAction(_ -> removeFile());
        btnToolBarFolderAdd.setOnAction(_ -> addFolder());
        btnToolBarFolderRemove.setOnAction(_ -> removeFolder());
    }

    private void addFile() {
        TreeItem<String> selected = resourceTree.getSelectionModel().getSelectedItem();
        if (selected == null)
            selected = resourceTree.getRoot();

        TreeItem<String> newFile = new TreeItem<>("New File");
        selected.getChildren().add(newFile);
        selected.setExpanded(true);
        statusLabel.setText("New file added");
    }

    private void removeFile() {
        TreeItem<String> selected = resourceTree.getSelectionModel().getSelectedItem();
        if (selected != null && selected != resourceTree.getRoot() && selected.getChildren().isEmpty()) {
            selected.getParent().getChildren().remove(selected);
            statusLabel.setText("File removed");
        } else {
            statusLabel.setText("Select a file to remove");
        }
    }

    private void addFolder() {
        TreeItem<String> selected = resourceTree.getSelectionModel().getSelectedItem();
        if (selected == null)
            selected = resourceTree.getRoot();

        TreeItem<String> newFolder = new TreeItem<>("New Folder");
        selected.getChildren().add(newFolder);
        selected.setExpanded(true);

        statusLabel.setText("New folder added");
    }

    private void removeFolder() {
        TreeItem<String> selected = resourceTree.getSelectionModel().getSelectedItem();
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

    private void updateInfoPanel(TreeItem<String> selected) {
        infoPanel.getChildren().clear();
        if (selected != null) {
            Label nameLabel = new Label("Name: " + selected.getValue());
            Label typeLabel = new Label(selected.getChildren().isEmpty() ? "Type: File" : "Type: Folder");
            infoPanel.getChildren().addAll(nameLabel, typeLabel);
        }
    }
}
