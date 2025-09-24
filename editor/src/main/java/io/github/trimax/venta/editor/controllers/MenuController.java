package io.github.trimax.venta.editor.controllers;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.editor.events.tree.TreeSelectEvent;
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
import io.github.trimax.venta.editor.utils.EventUtil;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

public final class MenuController {
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
        EventUtil.register(this);

        bindMenu();
    }

    private void bindMenu() {
        btnMenuEditorAbout.setOnAction(new EditorAboutHandler());
        btnMenuEditorQuit.setOnAction(new EditorQuitHandler());

        btnMenuArchiveNew.setOnAction(new ArchiveNewHandler());
        btnMenuArchiveSave.setOnAction(new ArchiveSaveHandler());
        btnMenuArchiveLoad.setOnAction(new ArchiveLoadHandler());
        btnMenuArchiveBuild.setOnAction(new ArchiveBuildHandler());

        btnMenuGroupAdd.setOnAction(new GroupAddHandler());
        btnMenuGroupRemove.setOnAction(new GroupRemoveHandler());

        btnMenuResourceAdd.setOnAction(new ResourceAddHandler());
        btnMenuResourceRemove.setOnAction(new ResourceRemoveHandler());
    }

    @Subscribe
    public void onTreeItemSelected(final TreeSelectEvent event) {
        btnMenuResourceAdd.setDisable(!event.hasSelected() || !event.getItem().type().isContainer());
        btnMenuResourceRemove.setDisable(!event.hasSelected() || event.getItem().type().isContainer());

        btnMenuGroupAdd.setDisable(!event.hasSelected() || !event.getItem().type().isContainer());
        btnMenuGroupRemove.setDisable(!event.hasSelected() || !event.getItem().type().isContainer() || !event.getItem().deletable());
    }
}
