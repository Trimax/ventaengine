package io.github.trimax.venta.editor.controllers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.handlers.archive.ArchiveBuildHandler;
import io.github.trimax.venta.editor.handlers.archive.ArchiveLoadHandler;
import io.github.trimax.venta.editor.handlers.archive.ArchiveNewHandler;
import io.github.trimax.venta.editor.handlers.archive.ArchiveSaveHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToolbarController {
    private final ArchiveNewHandler archiveNewHandler;
    private final ArchiveSaveHandler archiveSaveHandler;
    private final ArchiveLoadHandler archiveLoadHandler;
    private final ArchiveBuildHandler archiveBuildHandler;

    @FXML private Button btnToolBarGroupAudio;
    @FXML private Button btnToolBarGroupBillboard;
    @FXML private Button btnToolBarGroupCamera;
    @FXML private Button btnToolBarGroupCubeMap;
    @FXML private Button btnToolBarGroupEmitter;
    @FXML private Button btnToolBarGroupLight;
    @FXML private Button btnToolBarGroupMaterial;
    @FXML private Button btnToolBarGroupMesh;
    @FXML private Button btnToolBarGroupObject;
    @FXML private Button btnToolBarGroupProgram;
    @FXML private Button btnToolBarGroupScene;
    @FXML private Button btnToolBarGroupShader;
    @FXML private Button btnToolBarGroupSprite;
    @FXML private Button btnToolBarGroupGridMesh;
    @FXML private Button btnToolBarGroupTexture;

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
    public void onGroupAudio(final ActionEvent event) {

    }
}
