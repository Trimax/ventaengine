package io.github.trimax.venta.editor.controllers.heightmap;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.context.HeightmapContext;
import io.github.trimax.venta.editor.handlers.heightmap.HeightmapSaveHandler;
import io.github.trimax.venta.editor.model.event.heightmap.HeightmapGenerateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeightmapGeneratorController {
    private final HeightmapContext context;
    private final HeightmapSaveHandler saveHandler;

    @FXML
    public ImageView heightmapImageView;

    @FXML
    private void onSaveButton(final ActionEvent event){
        saveHandler.handle(event);
    }

    @FXML
    public void initialize() {
        EventUtil.post(new HeightmapGenerateEvent());
    }

    @FXML
    private void onSeedChanged(final InputEvent event) {
        Spinner<?> spinnerSeed = (Spinner<?>) event.getSource();
        context.setRandomSeed((int) spinnerSeed.getValue());
        EventUtil.post(new HeightmapGenerateEvent());
    }

    @FXML
    private void onAttenuationChanged(final InputEvent event) {
        Spinner<?> spinnerAttenuation = (Spinner<?>) event.getSource();
        context.setAttenuation((double) spinnerAttenuation.getValue());
        EventUtil.post(new HeightmapGenerateEvent());
    }

    @FXML
    private void onLevelsChanged(final InputEvent event) {
        Spinner<?> spinnerLevels = (Spinner<?>) event.getSource();
        context.setLevels((int) spinnerLevels.getValue());
        EventUtil.post(new HeightmapGenerateEvent());
    }

    @FXML
    public void onGroovyChanged(final ActionEvent event) {
        CheckBox checkBox = (CheckBox) event.getSource();
        context.setGroovy(checkBox.isSelected());
        EventUtil.post(new HeightmapGenerateEvent());        
    }

    @FXML
    public void onWidthChanged(final InputEvent event) {
        Spinner<?> spinnerWidth = (Spinner<?>) event.getSource();
        context.setWidth((int) spinnerWidth.getValue());
        EventUtil.post(new HeightmapGenerateEvent());
    }

    @FXML
    public void onHeightChanged(final InputEvent event) {
        Spinner<?> spinnerHeight = (Spinner<?>) event.getSource();
        context.setHeight((int) spinnerHeight.getValue());
        EventUtil.post(new HeightmapGenerateEvent());
    }

    @FXML
    public void onCellSizeChanged(final InputEvent event) {
        Spinner<?> spinnerCellSize = (Spinner<?>) event.getSource();
        context.setCellSize((double) spinnerCellSize.getValue());
        EventUtil.post(new HeightmapGenerateEvent());
    }
}