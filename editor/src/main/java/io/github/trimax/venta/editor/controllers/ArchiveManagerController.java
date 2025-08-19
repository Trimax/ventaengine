package io.github.trimax.venta.editor.controllers;

import io.github.trimax.venta.editor.handlers.*;
import io.github.trimax.venta.editor.model.Item;
import io.github.trimax.venta.editor.utils.TreeUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

public final class ArchiveManagerController {
    @FXML private TreeView<Item> tree;
    @FXML private Label status;
    @FXML private VBox info;

    @FXML private Button btnToolBarArchiveNew;

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
        TreeUtil.initialize(tree, info);

        bindToolBar();
        bindMenu();
    }

    private void bindMenu() {
        btnMenuFileAdd.setOnAction(new FileAddHandler(tree, status));
        btnMenuFileRemove.setOnAction(new FileRemoveHandler(tree, status));
        btnMenuFolderAdd.setOnAction(new FolderAddHandler(tree, status));
        btnMenuFolderRemove.setOnAction(new FolderRemoveHandler(tree, status));
    }

    private void bindToolBar() {
        btnToolBarArchiveNew.setOnAction(new ArchiveNewHandler(tree, info));

        btnToolBarFileAdd.setOnAction(new FileAddHandler(tree, status));
        btnToolBarFileRemove.setOnAction(new FileRemoveHandler(tree, status));
        btnToolBarFolderAdd.setOnAction(new FolderAddHandler(tree, status));
        btnToolBarFolderRemove.setOnAction(new FolderRemoveHandler(tree, status));
    }
}
