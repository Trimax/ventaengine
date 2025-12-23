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

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeightmapGeneratorController {
    private final HeightmapContext context;
    private final HeightmapSaveHandler saveHandler;

    @FXML
    public ImageView imgHeightmap;

    @FXML
    private void onSaveButton(final ActionEvent event) {
        saveHandler.handle(event);
    }

    @FXML
    public void initialize() {
        EventUtil.post(new HeightmapGenerateEvent());
    }

    @FXML
    private void onSeedChanged(final InputEvent event) {
        updateSpinnerValue(event, context::setRandomSeed);
    }

    @FXML
    private void onAttenuationChanged(final InputEvent event) {
        updateSpinnerValue(event, context::setAttenuation);
    }

    @FXML
    private void onLevelsChanged(final InputEvent event) {
        updateSpinnerValue(event, context::setLevels);
    }

    @FXML
    public void onGroovyChanged(final ActionEvent event) {
        final var checkBox = (CheckBox) event.getSource();
        context.setGroovy(checkBox.isSelected());
        EventUtil.post(new HeightmapGenerateEvent());        
    }

    @FXML
    public void onWidthChanged(final InputEvent event) {
        updateSpinnerValue(event, context::setWidth);
    }

    @FXML
    public void onHeightChanged(final InputEvent event) {
        updateSpinnerValue(event, context::setHeight);
    }

    @FXML
    public void onCellSizeChanged(final InputEvent event) {
        updateSpinnerValue(event, context::setCellSize);
    }

    private <T> void updateSpinnerValue(final InputEvent event, final Consumer<T> setter) {
        setter.accept(((Spinner<T>) event.getSource()).getValue());
        EventUtil.post(new HeightmapGenerateEvent());
    }
}