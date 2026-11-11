package io.github.trimax.venta.engine.factories;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.AbstractController;
import io.github.trimax.venta.engine.controllers.AudioController;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.controllers.EngineController;
import io.github.trimax.venta.engine.controllers.KeyboardController;
import io.github.trimax.venta.engine.controllers.TextController;
import io.github.trimax.venta.engine.controllers.WindowController;
import one.util.streamex.StreamEx;

import java.util.List;

@Component
public final class ControllerFactory extends AbstractFactory<AbstractController<?, ?>> {
    /** Resources are released before the window (GL context) and the audio device (AL context) are destroyed */
    private static final List<Class<?>> CLEANUP_ORDER = List.of(TextController.class, ConsoleController.class,
            EngineController.class, WindowController.class, KeyboardController.class, AudioController.class);

    private ControllerFactory(final List<AbstractController<?, ?>> controllers) {
        super(StreamEx.of(controllers).sortedByInt(controller -> CLEANUP_ORDER.indexOf(controller.getClass())).toList(),
                AbstractController::deinitialize);
    }
}
