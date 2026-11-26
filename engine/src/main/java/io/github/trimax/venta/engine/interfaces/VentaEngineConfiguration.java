package io.github.trimax.venta.engine.interfaces;

import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import lombok.NonNull;

public interface VentaEngineConfiguration {
    VentaEngineConfiguration DEFAULT = new VentaEngineConfiguration() {};

    default @NonNull RenderConfiguration getRenderConfiguration() {
        return RenderConfiguration.DEFAULT;
    }

    default @NonNull WindowConfiguration getWindowConfiguration() {
        return WindowConfiguration.DEFAULT;
    }

    /**
     * Whether the engine is a part of another application (an editor): its window is hidden, it is not swapped by the screen
     * refresh, and the host draws the frames one by one with {@link io.github.trimax.venta.engine.core.Engine#renderFrame()}.
     */
    default boolean isEmbedded() {
        return false;
    }

    record RenderConfiguration(boolean isConsoleEnabled,
                               boolean isDebugEnabled,
                               boolean isMouseCursorVisible,
                               boolean isVerticalSynchronizationEnabled,
                               AntialiasingSamples antialiasingSamples) {
        public static final RenderConfiguration DEFAULT = new RenderConfiguration(false, false, true, true, AntialiasingSamples.X4);
    }

    record WindowConfiguration(String title, int width, int height, boolean isFullscreen) {
        public static final WindowConfiguration DEFAULT = new WindowConfiguration("Venta Engine", 800, 600, false);
    }
}
