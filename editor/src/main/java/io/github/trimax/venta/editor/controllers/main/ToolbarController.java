package io.github.trimax.venta.editor.controllers.main;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.handlers.archive.ArchiveBuildHandler;
import io.github.trimax.venta.editor.handlers.archive.ArchiveLoadHandler;
import io.github.trimax.venta.editor.handlers.archive.ArchiveNewHandler;
import io.github.trimax.venta.editor.handlers.archive.ArchiveSaveHandler;
import io.github.trimax.venta.editor.model.event.group.GroupSelectEvent;
import io.github.trimax.venta.engine.enums.GroupType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToolbarController {
    private final ArchiveNewHandler archiveNewHandler;
    private final ArchiveSaveHandler archiveSaveHandler;
    private final ArchiveLoadHandler archiveLoadHandler;
    private final ArchiveBuildHandler archiveBuildHandler;

    @FXML private ToggleGroup groupSelector;

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
    public void initialize() {
        groupSelector.selectedToggleProperty().addListener((_, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                groupSelector.selectToggle(oldToggle);
                return;
            }

            if (newToggle != null)
                EventUtil.post(new GroupSelectEvent(GroupType.valueOf((String) newToggle.getUserData())));
        });
    }

    public void selectGroup(@NonNull final GroupType type) {
        for (final var toggle : groupSelector.getToggles())
            if (type.name().equalsIgnoreCase((String) toggle.getUserData())) {
                groupSelector.selectToggle(toggle);
                break;
            }
    }
}
