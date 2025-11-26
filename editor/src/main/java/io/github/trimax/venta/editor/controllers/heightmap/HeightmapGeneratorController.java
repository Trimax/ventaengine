package io.github.trimax.venta.editor.controllers.heightmap;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.utils.DialogUtil;
import io.github.trimax.venta.engine.utils.PerlinNoise;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeightmapGeneratorController implements Initializable {
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
    private CheckBox colorCheckBox;
    @FXML
    private CheckBox alphaCheckBox;

    @FXML
    private Button generateButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button resetButton;

    @FXML
    private ImageView heightmapImageView;
    @FXML
    private Label placeholderLabel;

    private float[][] currentHeightmap;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        initializeSpinners();
        initializeButtons();
        resetParameters();
    }

    private void initializeSpinners() {
        randomSeedSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999999, 12345, 1));
        widthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(400, 800, 512, 64));
        heightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(400, 800, 512, 64));
        cellSizeSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 100.0, 16.0, 1.0));
        levelsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 4, 1));
        attenuationSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 1.0, 0.5, 0.1));

        groovyCheckBox.setSelected(false);
        colorCheckBox.setSelected(false);
        alphaCheckBox.setSelected(false);
    }

    private void initializeButtons() {
        generateButton.setOnAction(this::onGenerateButtonClicked);
        saveButton.setOnAction(this::onSaveButtonClicked);
        resetButton.setOnAction(this::onResetButtonClicked);
        saveButton.setDisable(true);
    }

    private void onGenerateButtonClicked(final @NonNull ActionEvent event) {
        try {
            generateButton.setDisable(true);

            currentHeightmap = PerlinNoise.generateHeightmapWithParameters(
                    widthSpinner.getValue(), heightSpinner.getValue(),
                    randomSeedSpinner.getValue(), cellSizeSpinner.getValue(),
                    levelsSpinner.getValue(), attenuationSpinner.getValue(),
                    groovyCheckBox.isSelected() ? 1.0 : 0.0);

            final var heightmapImage = createImageFromHeightmap(currentHeightmap,
                    colorCheckBox.isSelected(), alphaCheckBox.isSelected());
            heightmapImageView.setImage(heightmapImage);
            heightmapImageView.setFitWidth(heightmapImage.getWidth());
            heightmapImageView.setFitHeight(heightmapImage.getHeight());
            heightmapImageView.setPreserveRatio(false);
            placeholderLabel.setVisible(false);
            saveButton.setDisable(false);

        } catch (final Exception e) {
            throw new RuntimeException("Failed to generate heightmap: " + e);
        } finally {
            generateButton.setDisable(false);
        }
    }

    private void onSaveButtonClicked(final @NonNull ActionEvent event) {
        final var filters = java.util.Map.of("PNG Image (*.png)", java.util.List.of("*.png"));
        DialogUtil.showFileSave("Save Heightmap", this::saveHeightmapToFile, filters);
    }

    private void onResetButtonClicked(final @NonNull ActionEvent event) {
        resetParameters();
    }

    private void resetParameters() {
        randomSeedSpinner.getValueFactory().setValue(5);
        widthSpinner.getValueFactory().setValue(512);
        heightSpinner.getValueFactory().setValue(512);
        cellSizeSpinner.getValueFactory().setValue(16.0);
        levelsSpinner.getValueFactory().setValue(4);
        attenuationSpinner.getValueFactory().setValue(0.5);
        groovyCheckBox.setSelected(false);
        colorCheckBox.setSelected(false);
        alphaCheckBox.setSelected(false);

        heightmapImageView.setImage(null);
        currentHeightmap = null;
        placeholderLabel.setVisible(true);
        saveButton.setDisable(true);
    }

    private Image createImageFromHeightmap(final float[][] heightmap, final boolean useColor, final boolean useAlpha) {
        final var width = heightmap.length;
        final var height = heightmap[0].length;

        final var image = new WritableImage(width, height);
        final var pixelWriter = image.getPixelWriter();

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                final var value = heightmap[x][y] / 255.0;
                final var color = getColorForValue(value, useColor, useAlpha);
                pixelWriter.setColor(x, y, color);
            }

        return image;
    }

    private Color getColorForValue(final double value, final boolean useColor, final boolean useAlpha) {
        final var alpha = useAlpha ? 0.7 : 1.0;

        if (useColor)
            return Color.color(value, value * 0.5, 0, alpha);
        else
            return Color.color(value, value, value, alpha);
    }

    private void saveHeightmapToFile(final @NonNull File file) {
        if (currentHeightmap == null || !file.getName().toLowerCase().endsWith(".png"))
            return;

        try {
            saveAsPNG(file);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to save heightmap: " + e);
        }
    }

    private void saveAsPNG(final @NonNull File file) throws Exception {
        final var width = currentHeightmap.length;
        final var height = currentHeightmap[0].length;

        final var bufferedImage = new java.awt.image.BufferedImage(
                width, height, java.awt.image.BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                final var grayValue = (int) currentHeightmap[x][y];
                final var rgb = (grayValue << 16) | (grayValue << 8) | grayValue;
                bufferedImage.setRGB(x, y, rgb);
            }

        ImageIO.write(bufferedImage, "PNG", file);
    }
}