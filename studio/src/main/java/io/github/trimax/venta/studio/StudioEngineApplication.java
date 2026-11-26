package io.github.trimax.venta.studio;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The engine of the viewport: a hidden window of the size of the viewport. The scene is the one the user opens, the camera is the
 * viewport's.
 */
@Slf4j
@RequiredArgsConstructor
final class StudioEngineApplication implements VentaEngineApplication {
    static final int INITIAL_WIDTH = 1280;
    static final int INITIAL_HEIGHT = 720;

    private final FlyCamera flyCamera;

    @Override
    public @NonNull VentaEngineConfiguration getConfiguration() {
        return new VentaEngineConfiguration() {
            @Override
            public @NonNull RenderConfiguration getRenderConfiguration() {
                return new RenderConfiguration(false, false, true, false, AntialiasingSamples.X4);
            }

            @Override
            public @NonNull WindowConfiguration getWindowConfiguration() {
                return new WindowConfiguration("Venta Studio", INITIAL_WIDTH, INITIAL_HEIGHT, false);
            }

            @Override
            public boolean isEmbedded() {
                return true;
            }
        };
    }

    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new VentaEngineStartupHandler() {
            @Override
            public void onStartup(final String[] args, final VentaContext context) {
                flyCamera.apply(context.getCameraManager().getCurrent());
                log.info("The viewport is ready");
            }
        };
    }
}
