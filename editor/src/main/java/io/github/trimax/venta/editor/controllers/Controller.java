package io.github.trimax.venta.editor.controllers;

import io.github.trimax.venta.editor.utils.EventUtil;
import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Controller {
    @FXML
    public void initialize() {
        EventUtil.register(this);
    }


}
