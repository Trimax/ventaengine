package io.github.trimax.venta.editor.controllers;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.events.archive.ArchiveBuildEvent;
import io.github.trimax.venta.editor.events.archive.ArchiveLoadEvent;
import io.github.trimax.venta.editor.events.archive.ArchiveSaveEvent;
import io.github.trimax.venta.editor.events.group.GroupAddEvent;
import io.github.trimax.venta.editor.events.group.GroupRemoveEvent;
import io.github.trimax.venta.editor.events.resource.ResourceAddEvent;
import io.github.trimax.venta.editor.events.resource.ResourceRemoveEvent;
import io.github.trimax.venta.editor.events.tree.TreeResetEvent;
import io.github.trimax.venta.editor.events.tree.TreeSelectEvent;
import io.github.trimax.venta.editor.handlers.group.GroupAddHandler;
import io.github.trimax.venta.editor.handlers.group.GroupRemoveHandler;
import io.github.trimax.venta.editor.handlers.resource.ResourceAddHandler;
import io.github.trimax.venta.editor.handlers.resource.ResourceRemoveHandler;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.services.ArchiveService;
import io.github.trimax.venta.editor.services.GroupService;
import io.github.trimax.venta.editor.services.ResourceService;
import io.github.trimax.venta.editor.utils.TreeUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeView;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
public final class TreeController {
    private final ResourceService resourceService;
    private final ArchiveService archiveService;
    private final GroupService groupService;

    private final GroupAddHandler groupAddHandler;
    private final GroupRemoveHandler groupRemoveHandler;

    private final ResourceAddHandler resourceAddHandler;
    private final ResourceRemoveHandler resourceRemoveHandler;

    @FXML private TreeView<Item> tree;

    @FXML private Button btnTreeResourceAdd;
    @FXML private Button btnTreeResourceRemove;
    @FXML private Button btnTreeFolderAdd;
    @FXML private Button btnTreeFolderRemove;

    @FXML
    public void initialize() {
        TreeUtil.initialize(tree);
    }

    @Subscribe
    public void onTreeReset(final TreeResetEvent ignored) {
        archiveService.reset(tree);
    }

    @Subscribe
    @SneakyThrows
    public void onArchiveSave(final ArchiveSaveEvent event) {
        archiveService.save(event.file(), tree.getRoot());
    }

    @Subscribe
    public void onArchiveLoad(final ArchiveLoadEvent event) {
        TreeUtil.initialize(tree);
        archiveService.load(event.file(), tree.getRoot());
    }

    @Subscribe
    @SneakyThrows
    public void onArchiveBuild(final ArchiveBuildEvent event) {
        archiveService.build(event.file(), tree.getRoot());
    }

    @FXML
    public void onGroupAdd(final ActionEvent event) {
        groupAddHandler.handle(event);
    }

    @FXML
    public void onGroupRemove(final ActionEvent event) {
        groupRemoveHandler.handle(event);
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
    public void handleGroupAdd(final GroupAddEvent event) {
        groupService.add(event.name(), tree.getSelectionModel().getSelectedItem());
    }

    @Subscribe
    public void handleGroupRemove(final GroupRemoveEvent event) {
        groupService.remove(tree.getSelectionModel().getSelectedItem());
    }

    @Subscribe
    public void handleResourceAdd(final ResourceAddEvent event) {
        resourceService.add(event.file(), tree.getSelectionModel().getSelectedItem());
    }

    @Subscribe
    public void handleResourceRemove(final ResourceRemoveEvent event) {
        resourceService.remove(tree.getSelectionModel().getSelectedItem());
    }

    @Subscribe
    public void onTreeItemSelected(final TreeSelectEvent event) {
        btnTreeResourceAdd.setDisable(!event.hasSelected()  || !event.getItem().type().isContainer());
        btnTreeResourceRemove.setDisable(!event.hasSelected() || event.getItem().type().isContainer());

        btnTreeFolderAdd.setDisable(!event.hasSelected() || !event.getItem().type().isContainer());
        btnTreeFolderRemove.setDisable(!event.hasSelected() || !event.getItem().type().isContainer() || !event.getItem().deletable());
    }
}
