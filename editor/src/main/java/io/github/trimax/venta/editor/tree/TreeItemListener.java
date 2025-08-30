package io.github.trimax.venta.editor.tree;

import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.model.tree.ResourceType;
import io.github.trimax.venta.editor.model.ui.Menu;
import io.github.trimax.venta.editor.model.ui.ToolBar;
import io.github.trimax.venta.editor.renderers.TextFileRenderer;
import io.github.trimax.venta.editor.renderers.TextureRenderer;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.util.function.Consumer;


@AllArgsConstructor
public final class TreeItemListener implements Consumer<TreeItem<Item>> {
    private final ToolBar toolBar;
    private final Menu menu;
    private final VBox info;

    @Override
    public void accept(final TreeItem<Item> selected) {
        if (selected == null)
            return;

        toolBar.update(selected.getValue());
        menu.update(selected.getValue());
        updateInfoPanel(selected);
    }

    private void updateInfoPanel(final TreeItem<Item> selected) {
        info.getChildren().clear();
        if (!selected.getValue().deletable() || !selected.getChildren().isEmpty())
            showGroupInformation(selected);
        else
            showResourceInformation(selected);
    }

    private void showGroupInformation(final TreeItem<Item> node) {
        final var labelGroupName = new Label("Group: " + node.getValue().name());
        final var labelCountGroups = new Label("Groups: " + StreamEx.of(node.getChildren()).filter(this::hasChildren).count());
        final var labelCountResources = new Label("Resources: " + StreamEx.of(node.getChildren()).remove(this::hasChildren).count());

        labelGroupName.setStyle("-fx-font-weight: bold;");
        info.getChildren().addAll(labelGroupName, labelCountGroups, labelCountResources);
    }

    @SneakyThrows
    private void showResourceInformation(final TreeItem<Item> node) {
        final var type = getResourceType(node);

        final var labelResourceName = new Label("Resource: " + node.getValue().name());
        final var labelResourceType = new Label("Type: " + type.getDisplayName());

        labelResourceName.setStyle("-fx-font-weight: bold;");
        info.getChildren().addAll(labelResourceName, labelResourceType);

        final var file = new File(node.getValue().reference());
        if (!file.exists()) {
            info.getChildren().add(new Label(String.format("Resource file `%s` is not found", file.getAbsoluteFile())));
            return;
        }

        //TODO: Create automatic selection
        switch (type) {
            case Textures:
                new TextureRenderer(node, info).render(file);
                return;
            case Materials:
            case Programs:
            case Scenes:
            case Objects:
            case Meshes:
            case Lights:
            case Cubemaps:
            case Shaders:
                new TextFileRenderer(node, info).render(file);
                return;
            default:
                info.getChildren().add(new Label("the resource type is not supported"));
        }
    }

    private boolean hasChildren(final TreeItem<Item> node) {
        return CollectionUtils.isNotEmpty(node.getChildren());
    }

    private ResourceType getResourceType(final TreeItem<Item> node) {
        if (!node.getValue().deletable())
            return ResourceType.valueOf(node.getValue().name());

        return getResourceType(node.getParent());
    }
}
