package io.github.trimax.venta.editor.controllers.mixer;

import io.github.trimax.venta.container.annotations.Component;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SelectorController {
    @FXML private Button btnSelect;
    @FXML private ImageView imgTexture;
    @FXML private RadioButton redButton;
    @FXML private RadioButton greenButton;
    @FXML private RadioButton blueButton;
    @FXML private RadioButton alphaButton;
    @FXML private RadioButton valueButton;
    @FXML private TextField txtValue;
}
