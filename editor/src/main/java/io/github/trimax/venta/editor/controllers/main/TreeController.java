package io.github.trimax.venta.editor.controllers.main;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.handlers.folder.FolderAddHandler;
import io.github.trimax.venta.editor.handlers.folder.FolderRemoveHandler;
import io.github.trimax.venta.editor.handlers.resource.ResourceAddHandler;
import io.github.trimax.venta.editor.handlers.resource.ResourceRemoveHandler;
import io.github.trimax.venta.editor.model.event.tree.TreeSelectEvent;
import io.github.trimax.venta.editor.utils.TreeUtil;
import io.github.trimax.venta.engine.enums.ResourceType;
import io.github.trimax.venta.engine.model.common.resource.Resource;
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
    private final FolderAddHandler folderAddHandler;
    private final FolderRemoveHandler folderRemoveHandler;

    private final ResourceAddHandler resourceAddHandler;
    private final ResourceRemoveHandler resourceRemoveHandler;

    @Getter
    @FXML private TreeView<Resource> tree;

    @FXML private Button btnTreeResourceAdd;
    @FXML private Button btnTreeResourceRemove;
    @FXML private Button btnTreeFolderAdd;
    @FXML private Button btnTreeFolderRemove;

    @FXML
    public void initialize() {
        TreeUtil.initialize(tree);
    }

    @FXML
    public void onFolderAdd(final ActionEvent event) {
        folderAddHandler.handle(event);
    }

    @FXML
    public void onFolderRemove(final ActionEvent event) {
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
        btnTreeResourceAdd.setDisable(!event.hasSelected()  || !event.getItem().type().isContainer());
        btnTreeResourceRemove.setDisable(!event.hasSelected() || event.getItem().type().isContainer());

        btnTreeFolderAdd.setDisable(!event.hasSelected() || !event.getItem().type().isContainer());
        btnTreeFolderRemove.setDisable(!event.hasSelected() || !event.getItem().type().isContainer() || event.getItem().type() == ResourceType.Group);
    }

    public TreeItem<Resource> getSelectedNode() {
        return tree.getSelectionModel().getSelectedItem();
    }

    public TreeItem<Resource> getRoot() {
        return getTree().getRoot();
    }
}
