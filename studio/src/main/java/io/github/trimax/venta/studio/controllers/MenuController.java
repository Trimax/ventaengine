package io.github.trimax.venta.studio.controllers;

import java.io.File;
import java.nio.file.Path;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import com.google.common.eventbus.Subscribe;

import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.WireframeTarget;
import io.github.trimax.venta.studio.model.event.HistoryChangedEvent;
import io.github.trimax.venta.studio.model.event.OutlineSelectedEvent;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.model.event.WireframeChangedEvent;
import io.github.trimax.venta.studio.model.CameraView;
import io.github.trimax.venta.studio.model.event.ProjectOpenedEvent;
import io.github.trimax.venta.studio.model.event.StatusEvent;
import io.github.trimax.venta.studio.services.ProjectService;
import io.github.trimax.venta.studio.services.PlacementService;
import io.github.trimax.venta.studio.services.SceneEditService;
import io.github.trimax.venta.studio.services.SelectionService;
import io.github.trimax.venta.studio.services.SceneService;
import io.github.trimax.venta.studio.services.UnsavedChangesService;
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
    private final SceneEditService edits;
    private final PlacementService placement;
    private final SelectionService selection;
    private final UnsavedChangesService unsaved;

    @FXML private Menu menuRecent;
    @FXML private MenuItem menuSave;
    @FXML private MenuItem menuUndo;
    @FXML private MenuItem menuRedo;
    @FXML private MenuItem menuDuplicate;
    @FXML private MenuItem menuDelete;
    @FXML private Menu menuObject;
    @FXML private CheckMenuItem viewHierarchy;
    @FXML private CheckMenuItem viewInspector;
    @FXML private CheckMenuItem viewBottom;
    @FXML private Menu viewWireframe;
    @FXML private CheckMenuItem viewGrid;
    @FXML private CheckMenuItem viewMarks;

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
        if (unsaved.confirm())
            Platform.exit();
    }

    @FXML
    public void onSave(final ActionEvent event) {
        edits.save();
    }

    @FXML
    public void onCreate(final ActionEvent event) {
        final var item = (MenuItem) event.getSource();
        placement.chooseAndPlace(OutlineKind.valueOf((String) item.getUserData()));
    }

    @FXML
    public void onDuplicate(final ActionEvent event) {
        final var item = selection.getSelected();
        if (item != null && item.isThing())
            edits.duplicate(item.kind(), item.index());
    }

    @FXML
    public void onDelete(final ActionEvent event) {
        final var item = selection.getSelected();
        if (item != null && item.isThing())
            edits.delete(item.kind(), item.index());
    }

    /** What is chosen changed: only a thing of the scene can be copied and deleted */
    @Subscribe
    public void onOutlineSelected(final OutlineSelectedEvent event) {
        final var thing = event.item() != null && event.item().isThing();
        menuDuplicate.setDisable(!thing);
        menuDelete.setDisable(!thing);
    }

    @FXML
    public void onUndo(final ActionEvent event) {
        edits.undo();
    }

    @FXML
    public void onRedo(final ActionEvent event) {
        edits.redo();
    }

    /** What can be undone or redone changed: the menu says what it would do */
    @Subscribe
    public void onHistoryChanged(final HistoryChangedEvent event) {
        menuSave.setDisable(session.getScene() == null);
        menuObject.setDisable(session.getScene() == null);
        menuUndo.setDisable(event.undoLabel().isEmpty());
        menuUndo.setText(event.undoLabel().isEmpty() ? "Undo" : "Undo " + event.undoLabel());
        menuRedo.setDisable(event.redoLabel().isEmpty());
        menuRedo.setText(event.redoLabel().isEmpty() ? "Redo" : "Redo " + event.redoLabel());
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

    /** The choice is changed by the toolbar or by the scene that is open: the items show it */
    @Subscribe
    public void onWireframeChanged(final WireframeChangedEvent event) {
        viewWireframe.getItems().stream().filter(CheckMenuItem.class::isInstance).map(CheckMenuItem.class::cast)
                .forEach(item -> item.setSelected(session.getWireframe().contains(WireframeTarget.valueOf((String) item.getUserData()))));
    }

    @FXML
    public void onOverlay(final ActionEvent event) {
        session.getOverlay().setGrid(viewGrid.isSelected());
        session.getOverlay().setMarks(viewMarks.isSelected());
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
