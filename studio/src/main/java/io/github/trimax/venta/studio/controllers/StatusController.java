package io.github.trimax.venta.studio.controllers;

import com.google.common.eventbus.Subscribe;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.event.StatusEvent;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * The bar under the window: what has happened, and what the viewport costs.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatusController {
    private static final long REFRESH_NANOSECONDS = 500_000_000L;

    private final StudioSession session;

    @FXML private Label text;
    @FXML private Label statistics;

    @FXML
    public void initialize() {
        text.setText("Ready");

        new AnimationTimer() {
            private long last;

            @Override
            public void handle(final long now) {
                if (now - last < REFRESH_NANOSECONDS || session.getViewport() == null)
                    return;

                last = now;
                final var values = session.getViewport().getStatistics();
                statistics.setText(String.format("Viewport %s | %.0f fps | frame %.1f ms", values.getSize(), values.getFramesPerSecond(), values.getFrameMilliseconds()));
            }
        }.start();
    }

    @Subscribe
    public void onStatus(final StatusEvent event) {
        text.setText(event.text());
    }
}
