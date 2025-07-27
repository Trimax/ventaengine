package com.venta.engine.interfaces;

import lombok.NonNull;

public interface VentaEngineConfiguration {
    VentaEngineConfiguration DEFAULT = new VentaEngineConfiguration() {};

    default @NonNull RenderConfiguration getRenderConfiguration() {
        return RenderConfiguration.DEFAULT;
    }

    default @NonNull WindowConfiguration getWindowConfiguration() {
        return WindowConfiguration.DEFAULT;
    }

    record RenderConfiguration(boolean isConsoleEnabled, boolean isOriginVisible) {
        public static final RenderConfiguration DEFAULT = new RenderConfiguration(false, false);
    }

    record WindowConfiguration(String title, int width, int height, boolean isFullscreen) {
        public static final WindowConfiguration DEFAULT = new WindowConfiguration("Venta Engine", 800, 600, false);
    }
}
