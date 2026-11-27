package io.github.trimax.venta.studio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.github.trimax.venta.container.AbstractVentaApplication;
import io.github.trimax.venta.container.VentaApplication;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.studio.services.ConsoleLog;
import io.github.trimax.venta.studio.services.ProjectService;
import io.github.trimax.venta.studio.services.PlacementService;
import io.github.trimax.venta.studio.services.SceneEditService;
import io.github.trimax.venta.studio.services.SceneService;
import io.github.trimax.venta.engine.model.common.gizmo.GizmoMode;
import io.github.trimax.venta.studio.services.GizmoService;
import io.github.trimax.venta.studio.services.SelectionService;
import io.github.trimax.venta.studio.services.UnsavedChangesService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * The editor of the scenes: the engine draws the scene into the viewport of a JavaFX window, in the same process, and the panels
 * around it show the project, the hierarchy of the scene and what is chosen.
 *
 * <p>With {@code -Dstudio.selftest=N} it opens {@code -Dstudio.scene} of {@code -Dstudio.project}, runs N frames, pushes the camera with
 * synthetic keys and mouse drags, writes the picture of the window to {@code -Dstudio.shot=file.png}, prints what it measured and exits.
 */
@Slf4j
@Component
public final class StudioApplication extends Application implements AbstractVentaApplication<Void> {
    private static final int DEFAULT_WIDTH = 1440;
    private static final int DEFAULT_HEIGHT = 900;

    private static String[] arguments = new String[0];

    private Engine engine;
    private StudioSession session;
    private ViewportPanel viewport;

    @Override
    public void start(final Stage stage) throws IOException {
        VentaApplication.getComponent(ConsoleLog.class).install();

        engine = VentaApplication.getComponent(Engine.class);
        session = VentaApplication.getComponent(StudioSession.class);
        final var projects = VentaApplication.getComponent(ProjectService.class);
        projects.loadSettings();

        final var flyCamera = new FlyCamera();
        viewport = new ViewportPanel(engine, VentaApplication.getComponent(VentaContext.class), flyCamera);

        session.setStage(stage);
        session.setHostServices(getHostServices());
        session.setViewport(viewport);
        session.setCamera(flyCamera);

        engine.startEmbedded(arguments, new StudioEngineApplication(flyCamera), viewport::onFrame);
        engine.setOverlay(session.getOverlay());

        final var selection = VentaApplication.getComponent(SelectionService.class);
        viewport.setOnPick(selection::pick);
        viewport.setOnFocus(selection::focusSelection);

        final var gizmos = VentaApplication.getComponent(GizmoService.class);
        viewport.setGizmo(new ViewportPanel.Gizmo() {
            @Override
            public void hover(final float x, final float y) {
                gizmos.hover(x, y);
            }

            @Override
            public boolean press(final float x, final float y) {
                return gizmos.press(x, y);
            }

            @Override
            public void drag(final float x, final float y, final boolean snap) {
                gizmos.drag(x, y, snap);
            }

            @Override
            public void release() {
                gizmos.release();
            }
        });
        viewport.setOnTool(key -> gizmos.setMode(switch (key) {
            case W -> GizmoMode.Translate;
            case E -> GizmoMode.Rotate;
            case R -> GizmoMode.Scale;
            default -> null;
        }));

        final var edits = VentaApplication.getComponent(SceneEditService.class);
        final var placement = VentaApplication.getComponent(PlacementService.class);
        viewport.setOnDelete(() -> {
            final var item = selection.getSelected();
            if (item != null && item.isThing())
                edits.delete(item.kind(), item.index());
        });
        viewport.setDrop(placement::isPlaceable, (file, x, y) -> placement.place(file, x, y));

        final var loader = new FXMLLoader(StudioApplication.class.getResource("/layouts/main.fxml"));
        loader.setControllerFactory(VentaApplication::getComponent);
        final Parent root = loader.load();

        final var settings = session.getSettings();
        final var scene = new Scene(root, settings.windowWidth() > 0 ? settings.windowWidth() : DEFAULT_WIDTH,
                settings.windowHeight() > 0 ? settings.windowHeight() : DEFAULT_HEIGHT);
        scene.getStylesheets().add(StudioApplication.class.getResource("/styles/dark.css").toExternalForm());

        stage.setTitle("Venta Studio");
        stage.setScene(scene);

        // Changes that are not saved are asked about before the window goes
        final var unsaved = VentaApplication.getComponent(UnsavedChangesService.class);
        stage.setOnCloseRequest(event -> {
            if (!unsaved.confirm())
                event.consume();
        });

        stage.show();

        viewport.start();
        viewport.requestFocus();

        openProject(projects);

        final var selftest = Integer.getInteger("studio.selftest");
        if (selftest != null)
            new SelfTest(stage, viewport, flyCamera, selftest).start();
    }

    @Override
    public void start(final String[] args, final Void argument) {
        arguments = args;
        launch(StudioApplication.class, args);
    }

    @Override
    public void stop() {
        if (viewport != null)
            viewport.stop();

        if (session != null && session.getStage() != null && session.getStage().getScene() != null && !session.getStage().isIconified())
            VentaApplication.getComponent(ProjectService.class).saveWindow((int) session.getStage().getScene().getWidth(), (int) session.getStage().getScene().getHeight());

        if (engine != null)
            engine.shutdown();
    }

    /** The project that the user asked for, or the one that was open last; a scene of it opens when a self-test asks */
    private void openProject(final ProjectService projects) {
        var project = StudioLauncher.projectOf(arguments);
        if (project == null && session.getSettings().lastProject() != null && Files.isDirectory(Path.of(session.getSettings().lastProject())))
            project = Path.of(session.getSettings().lastProject());

        if (project == null || !projects.open(project))
            return;

        final var scene = System.getProperty("studio.scene");
        if (scene != null)
            VentaApplication.getComponent(SceneService.class).open(scene);
    }
}
