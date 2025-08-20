package io.github.trimax.venta.editor.controllers;

import io.github.trimax.venta.editor.handlers.archive.ArchiveBuildHandler;
import io.github.trimax.venta.editor.handlers.archive.ArchiveLoadHandler;
import io.github.trimax.venta.editor.handlers.archive.ArchiveNewHandler;
import io.github.trimax.venta.editor.handlers.archive.ArchiveSaveHandler;
import io.github.trimax.venta.editor.handlers.editor.EditorAboutHandler;
import io.github.trimax.venta.editor.handlers.editor.EditorQuitHandler;
import io.github.trimax.venta.editor.handlers.group.GroupAddHandler;
import io.github.trimax.venta.editor.handlers.group.GroupRemoveHandler;
import io.github.trimax.venta.editor.handlers.resource.ResourceAddHandler;
import io.github.trimax.venta.editor.handlers.resource.ResourceRemoveHandler;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.model.ui.Menu;
import io.github.trimax.venta.editor.model.ui.ToolBar;
import io.github.trimax.venta.editor.tree.TreeItemListener;
import io.github.trimax.venta.editor.utils.TreeUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

public final class Controller {
    @FXML private TreeView<Item> tree;
    @FXML private Label status;
    @FXML private VBox info;

    @FXML private Button btnToolBarArchiveNew;
    @FXML private Button btnToolBarArchiveSave;
    @FXML private Button btnToolBarArchiveLoad;
    @FXML private Button btnToolBarArchiveBuild;

    @FXML private Button btnToolBarResourceAdd;
    @FXML private Button btnToolBarResourceRemove;
    @FXML private Button btnToolBarGroupAdd;
    @FXML private Button btnToolBarGroupRemove;

    @FXML private MenuItem btnMenuEditorAbout;
    @FXML private MenuItem btnMenuEditorQuit;

    @FXML private MenuItem btnMenuArchiveNew;
    @FXML private MenuItem btnMenuArchiveSave;
    @FXML private MenuItem btnMenuArchiveLoad;
    @FXML private MenuItem btnMenuArchiveBuild;


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

        btnMenuArchiveNew.setOnAction(new ArchiveNewHandler(tree, createListener()));
        btnMenuArchiveSave.setOnAction(new ArchiveSaveHandler(tree, status));
        btnMenuArchiveLoad.setOnAction(new ArchiveLoadHandler(tree, status));
        btnMenuArchiveBuild.setOnAction(new ArchiveBuildHandler(tree, status));

        btnMenuGroupAdd.setOnAction(new GroupAddHandler(tree, status));
        btnMenuGroupRemove.setOnAction(new GroupRemoveHandler(tree, status));

        btnMenuResourceAdd.setOnAction(new ResourceAddHandler(tree, status));
        btnMenuResourceRemove.setOnAction(new ResourceRemoveHandler(tree, status));
    }

    private void bindToolBar() {
        btnToolBarArchiveNew.setOnAction(new ArchiveNewHandler(tree, createListener()));
        btnToolBarArchiveSave.setOnAction(new ArchiveSaveHandler(tree, status));
        btnToolBarArchiveLoad.setOnAction(new ArchiveLoadHandler(tree, status));
        btnToolBarArchiveBuild.setOnAction(new ArchiveBuildHandler(tree, status));

        btnToolBarResourceAdd.setOnAction(new ResourceAddHandler(tree, status));
        btnToolBarResourceRemove.setOnAction(new ResourceRemoveHandler(tree, status));

        btnToolBarGroupAdd.setOnAction(new GroupAddHandler(tree, status));
        btnToolBarGroupRemove.setOnAction(new GroupRemoveHandler(tree, status));
    }

    private TreeItemListener createListener() {
        return new TreeItemListener(
                ToolBar.builder()
                        .btnToolBarResourceAdd(btnToolBarResourceAdd)
                        .btnToolBarResourceRemove(btnToolBarResourceRemove)
                        .btnToolBarGroupAdd(btnToolBarGroupAdd)
                        .btnToolBarGroupRemove(btnToolBarGroupRemove)
                        .build(),
                Menu.builder()
                        .btnMenuResourceAdd(btnMenuResourceAdd)
                        .btnMenuResourceRemove(btnMenuResourceRemove)
                        .btnMenuGroupAdd(btnMenuGroupAdd)
                        .btnMenuGroupRemove(btnMenuGroupRemove)
                        .build(),
                info);
    }
}
