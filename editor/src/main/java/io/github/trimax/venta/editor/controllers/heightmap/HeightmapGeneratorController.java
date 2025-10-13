package io.github.trimax.venta.editor.controllers.heightmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.ByteBuffer;
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
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeightmapGeneratorController implements Initializable {
    @FXML private Spinner<Integer> widthSpinner;
    @FXML private Spinner<Integer> heightSpinner;
    @FXML private Spinner<Integer> octaveCountSpinner;
    @FXML private Spinner<Double> amplitudeSpinner;
    @FXML private Spinner<Double> persistenceSpinner;
    @FXML private Spinner<Double> weightSpinner;
    @FXML private Spinner<Integer> maxValueSpinner;
    @FXML private Spinner<Integer> minValueSpinner;
    @FXML private Spinner<Integer> thresholdsSpinner;

    @FXML private Button generateButton;
    @FXML private Button saveButton;
    @FXML private Button resetButton;

    @FXML private ImageView heightmapImageView;
    @FXML private Label statusLabel;
    @FXML private Label placeholderLabel;
    
    private float[][] currentHeightmap;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        initializeSpinners();
        initializeButtons();
        resetParameters();
    }

    private void initializeSpinners() {
        widthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(400, 800, 512, 64));
        heightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(400, 800, 512, 64));
        octaveCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 8, 6, 1));
        amplitudeSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 10.0, 1.0, 1.0));
        persistenceSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.2, 0.1));
        weightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 100.0, 1.0, 1.0));
        minValueSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0, 1));
        maxValueSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 255, 1));
        thresholdsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 255, 4, 1));
    }

    private void initializeButtons() {
        generateButton.setOnAction(this::onGenerateButtonClicked);
        saveButton.setOnAction(this::onSaveButtonClicked);
        resetButton.setOnAction(this::onResetButtonClicked);
        saveButton.setDisable(true);
    }

    private void onGenerateButtonClicked(final @NonNull ActionEvent event) {
        try {
            statusLabel.setText("Generating heightmap...");
            generateButton.setDisable(true);

            currentHeightmap = PerlinNoise.generateHeightmap(
                widthSpinner.getValue(), heightSpinner.getValue(),
                octaveCountSpinner.getValue(), amplitudeSpinner.getValue(),
                persistenceSpinner.getValue(), weightSpinner.getValue(),
                minValueSpinner.getValue(), maxValueSpinner.getValue());

            final var heightmapImage = createImageFromHeightmap(currentHeightmap);
            heightmapImageView.setImage(heightmapImage);
            heightmapImageView.setFitWidth(heightmapImage.getWidth());
            heightmapImageView.setFitHeight(heightmapImage.getHeight());
            heightmapImageView.setPreserveRatio(false);
            placeholderLabel.setVisible(false);
            saveButton.setDisable(false);
            statusLabel.setText("Heightmap generated successfully");

        } catch (final Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        } finally {
            generateButton.setDisable(false);
        }
    }

    private void onSaveButtonClicked(final @NonNull ActionEvent event) {
        final var filters = java.util.Map.of(
                "PNG Image (*.png)", java.util.List.of("*.png"),
                "Raw Height Data (*.raw)", java.util.List.of("*.raw"),
                "Text Data (*.txt)", java.util.List.of("*.txt")
        );
        DialogUtil.showFileSave("Save Heightmap", this::saveHeightmapToFile, filters);
    }

    private void onResetButtonClicked(final @NonNull ActionEvent event) {
        resetParameters();
        statusLabel.setText("Parameters reset to defaults");
    }

    private void resetParameters() {
        widthSpinner.getValueFactory().setValue(512);
        heightSpinner.getValueFactory().setValue(512);
        octaveCountSpinner.getValueFactory().setValue(4);
        amplitudeSpinner.getValueFactory().setValue(1.0);
        persistenceSpinner.getValueFactory().setValue(0.6);
        weightSpinner.getValueFactory().setValue(2.0);
        minValueSpinner.getValueFactory().setValue(0);
        maxValueSpinner.getValueFactory().setValue(255);
        thresholdsSpinner.getValueFactory().setValue(4);

        heightmapImageView.setImage(null);
        currentHeightmap = null;
        placeholderLabel.setVisible(true);
        saveButton.setDisable(true);
    }

    private Image createImageFromHeightmap(final float[][] heightmap) {
        final int width = heightmap.length;
        final int height = heightmap[0].length;

        final WritableImage image = new WritableImage(width, height);
        final PixelWriter pixelWriter = image.getPixelWriter();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                final int grayValue = (int) heightmap[x][y];
                final Color color = Color.rgb(grayValue, grayValue, grayValue);
                pixelWriter.setColor(x, y, color);
            }
        }

        return image;
    }
    
    private void saveHeightmapToFile(final @NonNull File file) {
        if (currentHeightmap == null) {
            statusLabel.setText("No heightmap to save");
            return;
        }
        
        try {
            final String fileName = file.getName().toLowerCase();
            
            if (fileName.endsWith(".png"))
                saveAsPNG(file);
            else if (fileName.endsWith(".raw"))
                saveAsRaw(file);
            else if (fileName.endsWith(".txt"))
                saveAsText(file);
            else {
                statusLabel.setText("Unsupported file format");
                return;
            }
            
            statusLabel.setText("Heightmap saved to: " + file.getName());
            
        } catch (final Exception e) {
            statusLabel.setText("Error saving file: " + e.getMessage());
        }
    }
    
    private void saveAsPNG(final @NonNull File file) throws Exception {
        final int width = currentHeightmap.length;
        final int height = currentHeightmap[0].length;
        
        final var bufferedImage = new java.awt.image.BufferedImage(
            width, height, java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                final int grayValue = (int) currentHeightmap[x][y];
                final int rgb = (grayValue << 16) | (grayValue << 8) | grayValue;
                bufferedImage.setRGB(x, y, rgb);
            }
        }
        
        ImageIO.write(bufferedImage, "PNG", file);
    }
    
    private void saveAsRaw(final @NonNull File file) throws Exception {
        try (final var fos = new FileOutputStream(file);
             final var channel = fos.getChannel()) {
            
            final int width = currentHeightmap.length;
            final int height = currentHeightmap[0].length;
            final var buffer = ByteBuffer.allocate(width * height * 4);
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    buffer.putFloat(currentHeightmap[x][y]);
                }
            }
            
            buffer.flip();
            channel.write(buffer);
        }
    }
    
    private void saveAsText(final @NonNull File file) throws Exception {
        try (final var writer = new PrintWriter(file)) {
            final int width = currentHeightmap.length;
            final int height = currentHeightmap[0].length;
            
            writer.println("# VentaEngine Heightmap");
            writer.println("# Width: " + width);
            writer.println("# Height: " + height);
            writer.println();
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    writer.printf("%.6f", currentHeightmap[x][y]);
                    if (x < width - 1) writer.print(" ");
                }
                writer.println();
            }
        }
    }
}
