package io.github.trimax.venta.studio.controllers;

import com.google.common.eventbus.Subscribe;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.event.ProjectOpenedEvent;
import io.github.trimax.venta.studio.model.event.SceneOpenedEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * The window: the viewport is put in its place, and the panels are shown and hidden.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MainController {
    private final StudioSession session;

    @FXML private SplitPane horizontal;
    @FXML private SplitPane vertical;
    @FXML private StackPane viewportHost;
    @FXML private Label viewportHint;

    @FXML private Node hierarchyPanel;
    @FXML private Node inspectorPanel;
    @FXML private Node bottomPanel;

    @FXML
    public void initialize() {
        viewportHost.getChildren().addFirst(session.getViewport());
        showHint(session.getProject() == null
                ? "Open a project: File, Open Project"
                : "Open a scene: double click it in the Project panel");
    }

    @Subscribe
    public void onProjectOpened(final ProjectOpenedEvent event) {
        if (session.getScene() == null)
            showHint("Open a scene: double click it in the Project panel");
    }

    @Subscribe
    public void onSceneOpened(final SceneOpenedEvent event) {
        viewportHint.setVisible(false);
    }

    /** Shows or hides the panel; what is hidden doesn't take place in the window */
    public void setPanelVisible(final Panel panel, final boolean visible) {
        final var pane = panel == Panel.Bottom ? vertical : horizontal;
        final var node = switch (panel) {
            case Hierarchy -> hierarchyPanel;
            case Inspector -> inspectorPanel;
            case Bottom -> bottomPanel;
        };

        if (!visible) {
            pane.getItems().remove(node);
        } else if (!pane.getItems().contains(node)) {
            final var index = panel == Panel.Hierarchy ? 0 : pane.getItems().size();
            pane.getItems().add(index, node);
        }
    }

    private void showHint(final String text) {
        viewportHint.setText(text);
        viewportHint.setVisible(true);
    }

    /**
     * The panels that can be hidden.
     */
    public enum Panel {
        Hierarchy, Inspector, Bottom
    }
}
