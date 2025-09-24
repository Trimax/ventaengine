package io.github.trimax.venta.editor.controllers;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.events.status.StatusSetEvent;
import io.github.trimax.venta.editor.events.tree.TreeSelectEvent;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.model.tree.ResourceType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;

@Component
public final class PropertiesController {
    @FXML private VBox properties;

    @Subscribe
    public void onTreeSelect(final TreeSelectEvent event) {
        properties.getChildren().clear();

        if (event.hasSelected())
            updateInfoPanel(event.node());
    }

    private void updateInfoPanel(final TreeItem<Item> selected) {
        if (!selected.getValue().deletable() || !selected.getValue().hasExistingReference())
            showGroupInformation(selected);
        else
            showResourceInformation(selected);

        EventUtil.post(new StatusSetEvent("Item `%s` selected", selected.getValue().name()));
    }

    private void showGroupInformation(final TreeItem<Item> node) {
        final var labelGroupName = new Label("Group: " + node.getValue().name());
        final var labelCountGroups = new Label("Groups: " + StreamEx.of(node.getChildren()).filter(this::isGroup).count());
        final var labelCountResources = new Label("Resources: " + StreamEx.of(node.getChildren()).remove(this::isGroup).count());

        labelGroupName.setStyle("-fx-font-weight: bold;");
        properties.getChildren().addAll(labelGroupName, labelCountGroups, labelCountResources);
    }

    @SneakyThrows
    private void showResourceInformation(final TreeItem<Item> node) {
        final var type = getResourceType(node);

        final var labelResourceName = new Label("Resource: " + node.getValue().name());
        final var labelResourceType = new Label("Type: " + type.getDisplayName());

        labelResourceName.setStyle("-fx-font-weight: bold;");
        properties.getChildren().addAll(labelResourceName, labelResourceType);

        final var file = new File(node.getValue().reference());
        if (!file.exists()) {
            properties.getChildren().add(new Label(String.format("Resource file `%s` is not found", file.getAbsoluteFile())));
            return;
        }

        type.render(node, properties, file);
    }

    private boolean isGroup(final TreeItem<Item> node) {
        return CollectionUtils.isNotEmpty(node.getChildren()) || !node.getValue().hasExistingReference();
    }

    private ResourceType getResourceType(final TreeItem<Item> node) {
        if (!node.getValue().deletable())
            return ResourceType.valueOf(node.getValue().name());

        return getResourceType(node.getParent());
    }
}
