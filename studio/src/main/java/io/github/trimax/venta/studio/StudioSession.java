package io.github.trimax.venta.studio;

import java.nio.file.Path;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.studio.model.settings.StudioSettings;
import javafx.application.HostServices;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * What the parts of the studio share: the window, the viewport and its camera, and the project and the scene that are open.
 * The application fills it when it starts, the panels and the services read it.
 */
@Getter
@Setter
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StudioSession {
    private Stage stage;
    private HostServices hostServices;
    private ViewportPanel viewport;
    private FlyCamera camera;
    private StudioSettings settings = StudioSettings.DEFAULT;

    /** The folder of the open project, null before one is open */
    private Path project;

    /**
     * The name of the scene that is open, like {@code beach.json}, null before one is open.
     */
    private String scene;

    /**
     * Whether the objects of the scene are drawn as lines.
     */
    private boolean wireframe;
}
