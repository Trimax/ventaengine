package io.github.trimax.venta.studio.controllers;

import com.google.common.eventbus.Subscribe;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoMode;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoSpace;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.CameraView;
import io.github.trimax.venta.studio.model.WireframeTarget;
import io.github.trimax.venta.studio.model.event.GizmoChangedEvent;
import io.github.trimax.venta.studio.model.event.WireframeChangedEvent;
import io.github.trimax.venta.studio.services.GizmoService;
import io.github.trimax.venta.studio.services.SceneService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
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
    private final GizmoService gizmos;

    @FXML private MenuButton wireframe;
    @FXML private ToggleButton selectTool;
    @FXML private ToggleButton moveTool;
    @FXML private ToggleButton rotateTool;
    @FXML private ToggleButton scaleTool;
    @FXML private ToggleButton space;

    @FXML
    public void onSelectTool(final ActionEvent event) {
        gizmos.setMode(null);
    }

    @FXML
    public void onMoveTool(final ActionEvent event) {
        gizmos.setMode(GizmoMode.Translate);
    }

    @FXML
    public void onRotateTool(final ActionEvent event) {
        gizmos.setMode(GizmoMode.Rotate);
    }

    @FXML
    public void onScaleTool(final ActionEvent event) {
        gizmos.setMode(GizmoMode.Scale);
    }

    @FXML
    public void onSpace(final ActionEvent event) {
        gizmos.setSpace(space.isSelected() ? GizmoSpace.Local : GizmoSpace.Global);
    }

    /** The tool is chosen by a key or by a button: the buttons show it (the chosen one stays down when it is pressed again) */
    @Subscribe
    public void onGizmoChanged(final GizmoChangedEvent event) {
        selectTool.setSelected(event.mode() == null);
        moveTool.setSelected(event.mode() == GizmoMode.Translate);
        rotateTool.setSelected(event.mode() == GizmoMode.Rotate);
        scaleTool.setSelected(event.mode() == GizmoMode.Scale);

        space.setSelected(event.space() == GizmoSpace.Local);
        space.setText(event.space() == GizmoSpace.Local ? "Local" : "Global");
    }

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
        final var item = (CheckMenuItem) event.getSource();
        scenes.setWireframe(WireframeTarget.valueOf((String) item.getUserData()), item.isSelected());
    }

    @FXML
    public void onWireframeAll(final ActionEvent event) {
        scenes.setWireframeEverywhere(true);
    }

    @FXML
    public void onWireframeNone(final ActionEvent event) {
        scenes.setWireframeEverywhere(false);
    }

    /** The choice is changed by the menu or by the scene that is open: the items show it */
    @Subscribe
    public void onWireframeChanged(final WireframeChangedEvent event) {
        wireframe.getItems().stream().filter(CheckMenuItem.class::isInstance).map(CheckMenuItem.class::cast)
                .forEach(item -> item.setSelected(session.getWireframe().contains(WireframeTarget.valueOf((String) item.getUserData()))));
    }
}
