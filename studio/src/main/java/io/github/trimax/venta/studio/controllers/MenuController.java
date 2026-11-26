package io.github.trimax.venta.studio.controllers;

import java.io.File;
import java.nio.file.Path;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.CameraView;
import io.github.trimax.venta.studio.model.event.ProjectOpenedEvent;
import io.github.trimax.venta.studio.model.event.StatusEvent;
import io.github.trimax.venta.studio.services.ProjectService;
import io.github.trimax.venta.studio.services.SceneService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.DirectoryChooser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The menu of the window. What isn't made yet is in the menu and disabled, so the whole of the studio is seen.
 */
@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MenuController {
    private static final String DOCUMENTATION = "https://github.com/Trimax/ventaengine";

    private final StudioSession session;
    private final ProjectService projects;
    private final SceneService scenes;
    private final MainController main;

    @FXML private Menu menuRecent;
    @FXML private CheckMenuItem viewHierarchy;
    @FXML private CheckMenuItem viewInspector;
    @FXML private CheckMenuItem viewBottom;
    @FXML private CheckMenuItem viewWireframe;

    @FXML
    public void initialize() {
        menuRecent.setOnShowing(event -> fillRecent());
        fillRecent();
    }

    @FXML
    public void onProjectOpen(final ActionEvent event) {
        final var chooser = new DirectoryChooser();
        chooser.setTitle("Open Project");

        final var last = session.getSettings().lastProject();
        if (last != null && new File(last).getParentFile() != null && new File(last).getParentFile().isDirectory())
            chooser.setInitialDirectory(new File(last).getParentFile());

        final var folder = chooser.showDialog(session.getStage());
        if (folder != null)
            projects.open(folder.toPath());
    }

    @FXML
    public void onQuit(final ActionEvent event) {
        Platform.exit();
    }

    @FXML
    public void onRefresh(final ActionEvent event) {
        if (session.getProject() != null)
            EventUtil.post(new ProjectOpenedEvent(session.getProject()));
    }

    @FXML
    public void onPanels(final ActionEvent event) {
        main.setPanelVisible(MainController.Panel.Hierarchy, viewHierarchy.isSelected());
        main.setPanelVisible(MainController.Panel.Inspector, viewInspector.isSelected());
        main.setPanelVisible(MainController.Panel.Bottom, viewBottom.isSelected());
    }

    @FXML
    public void onViewPerspective(final ActionEvent event) {
        session.getCamera().view(CameraView.Perspective);
    }

    @FXML
    public void onViewTop(final ActionEvent event) {
        session.getCamera().view(CameraView.Top);
    }

    @FXML
    public void onViewFront(final ActionEvent event) {
        session.getCamera().view(CameraView.Front);
    }

    @FXML
    public void onViewSide(final ActionEvent event) {
        session.getCamera().view(CameraView.Side);
    }

    @FXML
    public void onViewReset(final ActionEvent event) {
        session.getCamera().reset();
    }

    @FXML
    public void onWireframe(final ActionEvent event) {
        scenes.setWireframe(viewWireframe.isSelected());
    }

    @FXML
    public void onAbout(final ActionEvent event) {
        final var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Venta Studio");
        alert.setHeaderText("Venta Studio");
        alert.setContentText("The editor of the scenes of Venta Engine.\n" + DOCUMENTATION);
        alert.initOwner(session.getStage());
        alert.showAndWait();
    }

    @FXML
    public void onDocumentation(final ActionEvent event) {
        if (session.getHostServices() != null)
            session.getHostServices().showDocument(DOCUMENTATION);
    }

    @FXML
    public void onLogFolder(final ActionEvent event) {
        final var folder = Path.of(System.getProperty("user.dir"), "logs");
        if (session.getHostServices() != null)
            session.getHostServices().showDocument(folder.toUri().toString());
        else
            EventUtil.post(new StatusEvent("The logs are in %s", folder));
    }

    private void fillRecent() {
        menuRecent.getItems().clear();

        for (final var folder : session.getSettings().recentProjects()) {
            final var item = new MenuItem(folder);
            item.setOnAction(action -> projects.open(Path.of(folder)));
            menuRecent.getItems().add(item);
        }

        menuRecent.setDisable(menuRecent.getItems().isEmpty());
    }
}
