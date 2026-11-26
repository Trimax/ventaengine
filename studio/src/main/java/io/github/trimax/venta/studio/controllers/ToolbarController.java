package io.github.trimax.venta.studio.controllers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.CameraView;
import io.github.trimax.venta.studio.services.SceneService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * The buttons over the viewport: where the camera is, and how the objects are drawn.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToolbarController {
    private final StudioSession session;
    private final SceneService scenes;

    @FXML private ToggleButton wireframe;

    @FXML
    public void onPerspective(final ActionEvent event) {
        session.getCamera().view(CameraView.Perspective);
    }

    @FXML
    public void onTop(final ActionEvent event) {
        session.getCamera().view(CameraView.Top);
    }

    @FXML
    public void onFront(final ActionEvent event) {
        session.getCamera().view(CameraView.Front);
    }

    @FXML
    public void onSide(final ActionEvent event) {
        session.getCamera().view(CameraView.Side);
    }

    @FXML
    public void onReset(final ActionEvent event) {
        session.getCamera().reset();
    }

    @FXML
    public void onWireframe(final ActionEvent event) {
        scenes.setWireframe(wireframe.isSelected());
    }
}
