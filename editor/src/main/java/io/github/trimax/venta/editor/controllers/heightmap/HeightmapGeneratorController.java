package io.github.trimax.venta.editor.controllers.heightmap;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.context.HeightmapContext;
import io.github.trimax.venta.editor.handlers.heightmap.HeightmapSaveHandler;
import io.github.trimax.venta.editor.model.event.heightmap.HeightmapGenerateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
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
    private Spinner<Integer> randomSeedSpinner;
    @FXML
    private Spinner<Integer> widthSpinner;
    @FXML
    private Spinner<Integer> heightSpinner;
    @FXML
    private Spinner<Double> cellSizeSpinner;
    @FXML
    private Spinner<Integer> levelsSpinner;
    @FXML
    private Spinner<Double> attenuationSpinner;
    @FXML
    private CheckBox groovyCheckBox;

    @FXML
    private Button saveButton;

    @FXML
    public ImageView heightmapImageView;

    @FXML
    private Label placeholderLabel;

    @FXML
    private void onSeedChanged(InputEvent event) {
        Spinner<?> spinnerSeed = (Spinner<?>) event.getSource();
        context.setRandomSeed((int) spinnerSeed.getValue());
        EventUtil.post(new HeightmapGenerateEvent());
    }

    @FXML
    private void onSaveButton(){

    }

    @FXML
    public void initialize() {
        EventUtil.post(new HeightmapGenerateEvent());
    }
}