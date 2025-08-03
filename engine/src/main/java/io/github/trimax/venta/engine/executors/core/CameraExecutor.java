package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.executors.camera.AbstractCameraExecutor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class CameraExecutor extends AbstractCoreExecutor {
    private CameraExecutor(@NonNull final InternalVentaContext context, @NonNull final List<AbstractCameraExecutor> executors) {
        super(context, "camera", "the set of commands to manage cameras", executors);
    }
}
