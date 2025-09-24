package io.github.trimax.venta.editor.controllers;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.editor.events.archive.ArchiveBuildEvent;
import io.github.trimax.venta.editor.events.archive.ArchiveLoadEvent;
import io.github.trimax.venta.editor.events.archive.ArchiveSaveEvent;
import io.github.trimax.venta.editor.events.group.GroupAddEvent;
import io.github.trimax.venta.editor.events.group.GroupRemoveEvent;
import io.github.trimax.venta.editor.events.resource.ResourceAddEvent;
import io.github.trimax.venta.editor.events.resource.ResourceRemoveEvent;
import io.github.trimax.venta.editor.events.status.StatusSetEvent;
import io.github.trimax.venta.editor.events.tree.TreeResetEvent;
import io.github.trimax.venta.editor.events.tree.TreeSelectEvent;
import io.github.trimax.venta.editor.handlers.group.GroupAddHandler;
import io.github.trimax.venta.editor.handlers.group.GroupRemoveHandler;
import io.github.trimax.venta.editor.handlers.resource.ResourceAddHandler;
import io.github.trimax.venta.editor.handlers.resource.ResourceRemoveHandler;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.services.ArchiveService;
import io.github.trimax.venta.editor.services.GroupService;
import io.github.trimax.venta.editor.utils.EventUtil;
import io.github.trimax.venta.editor.utils.TreeUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.SneakyThrows;

public final class TreeController {
    @FXML private TreeView<Item> tree;

    @FXML private Button btnTreeResourceAdd;
    @FXML private Button btnTreeResourceRemove;
    @FXML private Button btnTreeFolderAdd;
    @FXML private Button btnTreeFolderRemove;

    @FXML
    public void initialize() {
        EventUtil.register(this);
        TreeUtil.initialize(tree);

        btnTreeResourceAdd.setOnAction(new ResourceAddHandler());
        btnTreeResourceRemove.setOnAction(new ResourceRemoveHandler());
        btnTreeFolderAdd.setOnAction(new GroupAddHandler());
        btnTreeFolderRemove.setOnAction(new GroupRemoveHandler());
    }

    @Subscribe
    public void onTreeReset(final TreeResetEvent ignored) {
        new ArchiveService().reset(tree);
    }

    @Subscribe
    @SneakyThrows
    public void onArchiveSave(final ArchiveSaveEvent event) {
        new ArchiveService().save(event.file(), tree.getRoot());
    }

    @Subscribe
    public void onArchiveLoad(final ArchiveLoadEvent event) {
        TreeUtil.initialize(tree);
        new ArchiveService().load(event.file(), tree.getRoot());
    }

    @Subscribe
    @SneakyThrows
    public void onArchiveBuild(final ArchiveBuildEvent event) {
        new ArchiveService().build(event.file(), tree.getRoot());
    }

    @Subscribe
    public void onTreeItemSelected(final TreeSelectEvent event) {
        final var item = event.node().getValue();
        btnTreeResourceAdd.setDisable(item == null || !item.type().isContainer());
        btnTreeResourceRemove.setDisable(item == null || item.type().isContainer());

        btnTreeFolderAdd.setDisable(item == null || !item.type().isContainer());
        btnTreeFolderRemove.setDisable(item == null || !item.type().isContainer() || !item.deletable());
    }

    @Subscribe
    public void onGroupAdd(final GroupAddEvent event) {
        new GroupService().add(event.name(), tree.getSelectionModel().getSelectedItem());
    }

    @Subscribe
    public void onGroupRemove(final GroupRemoveEvent event) {
        final var selected = tree.getSelectionModel().getSelectedItem();
        selected.getParent().getChildren().remove(selected);

        EventUtil.post(new StatusSetEvent("Group `%s` removed", selected.getValue().name()));
    }

    @Subscribe
    public void onResourceAdd(final ResourceAddEvent event) {
        final var selected = tree.getSelectionModel().getSelectedItem();
        final var resource = new TreeItem<>(Item.asResource(event.file().getName(), event.file().getAbsolutePath()));
        selected.getChildren().add(resource);
        selected.setExpanded(true);

        EventUtil.post(new StatusSetEvent("Resource `%s` added", event.file().getAbsolutePath()));
    }

    @Subscribe
    public void onResourceRemove(final ResourceRemoveEvent event) {
        final var selected = tree.getSelectionModel().getSelectedItem();
        selected.getParent().getChildren().remove(selected);

        EventUtil.post(new StatusSetEvent("Resource `%s` removed", selected.getValue().name()));
    }
}
