package io.github.trimax.venta.editor.controllers;

import io.github.trimax.venta.editor.handlers.archive.ArchiveNewHandler;
import io.github.trimax.venta.editor.handlers.editor.EditorAboutHandler;
import io.github.trimax.venta.editor.handlers.editor.EditorQuitHandler;
import io.github.trimax.venta.editor.handlers.group.GroupAddHandler;
import io.github.trimax.venta.editor.handlers.group.GroupRemoveHandler;
import io.github.trimax.venta.editor.handlers.resource.ResourceAddHandler;
import io.github.trimax.venta.editor.handlers.resource.ResourceRemoveHandler;
import io.github.trimax.venta.editor.model.Item;
import io.github.trimax.venta.editor.model.ToolBar;
import io.github.trimax.venta.editor.tree.TreeItemListener;
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

    @FXML private MenuItem btnMenuEditorAbout;
    @FXML private MenuItem btnMenuEditorQuit;

    @FXML private MenuItem btnMenuGroupAdd;
    @FXML private MenuItem btnMenuGroupRemove;

    @FXML private MenuItem btnMenuResourceAdd;
    @FXML private MenuItem btnMenuResourceRemove;

    @FXML
    public void initialize() {
        TreeUtil.initialize(tree, createListener());

        bindToolBar();
        bindMenu();
    }

    private void bindMenu() {
        btnMenuEditorAbout.setOnAction(new EditorAboutHandler());
        btnMenuEditorQuit.setOnAction(new EditorQuitHandler());

        btnMenuGroupAdd.setOnAction(new GroupAddHandler(tree, status));
        btnMenuGroupRemove.setOnAction(new GroupRemoveHandler(tree, status));

        btnMenuResourceAdd.setOnAction(new ResourceAddHandler(tree, status));
        btnMenuResourceRemove.setOnAction(new ResourceRemoveHandler(tree, status));
    }

    private void bindToolBar() {
        btnToolBarArchiveNew.setOnAction(new ArchiveNewHandler(tree, createListener()));

        btnToolBarFileAdd.setOnAction(new ResourceAddHandler(tree, status));
        btnToolBarFileRemove.setOnAction(new ResourceRemoveHandler(tree, status));
        btnToolBarFolderAdd.setOnAction(new GroupAddHandler(tree, status));
        btnToolBarFolderRemove.setOnAction(new GroupRemoveHandler(tree, status));
    }

    private TreeItemListener createListener() {
        return new TreeItemListener(ToolBar.builder()
                .btnToolBarArchiveNew(btnToolBarArchiveNew)
                .btnToolBarFileAdd(btnToolBarFileAdd)
                .btnToolBarFileRemove(btnToolBarFileRemove)
                .btnToolBarFolderAdd(btnToolBarFolderAdd)
                .btnToolBarFolderRemove(btnToolBarFolderRemove)
                .build(), info);
    }
}
