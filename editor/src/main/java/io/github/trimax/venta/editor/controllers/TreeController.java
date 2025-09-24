package io.github.trimax.venta.editor.controllers;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.events.tree.TreeSelectEvent;
import io.github.trimax.venta.editor.handlers.group.GroupAddHandler;
import io.github.trimax.venta.editor.handlers.group.GroupRemoveHandler;
import io.github.trimax.venta.editor.handlers.resource.ResourceAddHandler;
import io.github.trimax.venta.editor.handlers.resource.ResourceRemoveHandler;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.utils.TreeUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TreeController {
    private final GroupAddHandler groupAddHandler;
    private final GroupRemoveHandler groupRemoveHandler;

    private final ResourceAddHandler resourceAddHandler;
    private final ResourceRemoveHandler resourceRemoveHandler;

    @Getter
    @FXML private TreeView<Item> tree;

    @FXML private Button btnTreeResourceAdd;
    @FXML private Button btnTreeResourceRemove;
    @FXML private Button btnTreeFolderAdd;
    @FXML private Button btnTreeFolderRemove;

    @FXML
    public void initialize() {
        TreeUtil.initialize(tree);
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
    public void onTreeItemSelected(final TreeSelectEvent event) {
        btnTreeResourceAdd.setDisable(!event.hasSelected()  || !event.getItem().type().isContainer());
        btnTreeResourceRemove.setDisable(!event.hasSelected() || event.getItem().type().isContainer());

        btnTreeFolderAdd.setDisable(!event.hasSelected() || !event.getItem().type().isContainer());
        btnTreeFolderRemove.setDisable(!event.hasSelected() || !event.getItem().type().isContainer() || !event.getItem().deletable());
    }

    public TreeItem<Item> getSelectedNode() {
        return tree.getSelectionModel().getSelectedItem();
    }

    public TreeItem<Item> getRoot() {
        return getTree().getRoot();
    }
}
