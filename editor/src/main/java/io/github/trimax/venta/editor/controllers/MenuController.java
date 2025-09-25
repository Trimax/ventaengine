package io.github.trimax.venta.editor.controllers;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.handlers.archive.ArchiveBuildHandler;
import io.github.trimax.venta.editor.handlers.archive.ArchiveLoadHandler;
import io.github.trimax.venta.editor.handlers.archive.ArchiveNewHandler;
import io.github.trimax.venta.editor.handlers.archive.ArchiveSaveHandler;
import io.github.trimax.venta.editor.handlers.editor.EditorAboutHandler;
import io.github.trimax.venta.editor.handlers.editor.EditorQuitHandler;
import io.github.trimax.venta.editor.handlers.folder.FolderAddHandler;
import io.github.trimax.venta.editor.handlers.folder.FolderRemoveHandler;
import io.github.trimax.venta.editor.handlers.resource.ResourceAddHandler;
import io.github.trimax.venta.editor.handlers.resource.ResourceRemoveHandler;
import io.github.trimax.venta.editor.model.event.tree.TreeSelectEvent;
import io.github.trimax.venta.editor.model.tree.ItemType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MenuController {
    private final ArchiveNewHandler archiveNewHandler;
    private final ArchiveSaveHandler archiveSaveHandler;
    private final ArchiveLoadHandler archiveLoadHandler;
    private final ArchiveBuildHandler archiveBuildHandler;

    private final EditorAboutHandler editorAboutHandler;
    private final EditorQuitHandler editorQuitHandler;

    private final FolderAddHandler folderAddHandler;
    private final FolderRemoveHandler folderRemoveHandler;

    private final ResourceAddHandler resourceAddHandler;
    private final ResourceRemoveHandler resourceRemoveHandler;

    @FXML private MenuItem btnMenuGroupAdd;
    @FXML private MenuItem btnMenuGroupRemove;

    @FXML private MenuItem btnMenuResourceAdd;
    @FXML private MenuItem btnMenuResourceRemove;

    @FXML
    public void onArchiveNew(final ActionEvent event) {
        archiveNewHandler.handle(event);
    }

    @FXML
    public void onArchiveSave(final ActionEvent event) {
        archiveSaveHandler.handle(event);
    }

    @FXML
    public void onArchiveLoad(final ActionEvent event) {
        archiveLoadHandler.handle(event);
    }

    @FXML
    public void onArchiveBuild(final ActionEvent event) {
        archiveBuildHandler.handle(event);
    }

    @FXML
    public void onEditorAbout(final ActionEvent event) {
        editorAboutHandler.handle(event);
    }

    @FXML
    public void onEditorQuit(final ActionEvent event) {
        editorQuitHandler.handle(event);
    }

    @FXML
    public void onGroupAdd(final ActionEvent event) {
        folderAddHandler.handle(event);
    }

    @FXML
    public void onGroupRemove(final ActionEvent event) {
        folderRemoveHandler.handle(event);
    }

    @FXML
    public void onResourceAdd(final ActionEvent event) {
        resourceAddHandler.handle(event);
    }

    @FXML
    public void onResourceRemove(final ActionEvent event) {
        resourceRemoveHandler.handle(event);
    }

    @Subscribe
    public void onTreeItemSelected(final TreeSelectEvent event) {
        btnMenuResourceAdd.setDisable(!event.hasSelected() || !event.getItem().type().isContainer());
        btnMenuResourceRemove.setDisable(!event.hasSelected() || event.getItem().type().isContainer());

        btnMenuGroupAdd.setDisable(!event.hasSelected() || !event.getItem().type().isContainer());
        btnMenuGroupRemove.setDisable(!event.hasSelected() || !event.getItem().type().isContainer() || event.getItem().type() == ItemType.Group);
    }
}
