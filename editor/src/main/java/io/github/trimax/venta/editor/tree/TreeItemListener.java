package io.github.trimax.venta.editor.tree;

import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.model.tree.ResourceType;
import io.github.trimax.venta.editor.model.ui.Menu;
import io.github.trimax.venta.editor.model.ui.ToolBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
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

        switch (type) {
            case Textures:
                final var imageView = new ImageView(file.toURI().toString());
                imageView.setFitWidth(imageView.getImage().getWidth());
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(Math.min(imageView.getImage().getWidth(), 1024));

                final var image = imageView.getImage();
                final var labelTextureParameters = new Label(String.format("Width: %d; Height: %d", (int) image.getWidth(), (int) image.getHeight()));

                info.getChildren().addAll(labelTextureParameters, imageView);
                return;
            case Shaders:
                final var textShader = new TextArea( FileUtils.readFileToString(file, StandardCharsets.UTF_8));
                textShader.setEditable(false);
                textShader.setWrapText(true);
                textShader.setPrefRowCount(50);

                info.getChildren().add(textShader);
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
