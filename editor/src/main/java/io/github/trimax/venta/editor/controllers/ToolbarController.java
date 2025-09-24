package io.github.trimax.venta.editor.controllers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.handlers.archive.ArchiveBuildHandler;
import io.github.trimax.venta.editor.handlers.archive.ArchiveLoadHandler;
import io.github.trimax.venta.editor.handlers.archive.ArchiveNewHandler;
import io.github.trimax.venta.editor.handlers.archive.ArchiveSaveHandler;
import io.github.trimax.venta.editor.utils.EventUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

@Component
public final class ToolbarController {
    @FXML private Button btnToolBarArchiveNew;
    @FXML private Button btnToolBarArchiveSave;
    @FXML private Button btnToolBarArchiveLoad;
    @FXML private Button btnToolBarArchiveBuild;

    @FXML
    public void initialize() {
        EventUtil.register(this);

        btnToolBarArchiveNew.setOnAction(new ArchiveNewHandler());
        btnToolBarArchiveSave.setOnAction(new ArchiveSaveHandler());
        btnToolBarArchiveLoad.setOnAction(new ArchiveLoadHandler());
        btnToolBarArchiveBuild.setOnAction(new ArchiveBuildHandler());
    }
}
