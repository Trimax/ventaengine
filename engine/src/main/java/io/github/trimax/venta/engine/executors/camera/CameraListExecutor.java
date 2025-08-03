package io.github.trimax.venta.engine.executors.camera;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.managers.implementation.CameraManagerImplementation;
import io.github.trimax.venta.engine.model.view.AbstractView;
import io.github.trimax.venta.engine.utils.FormatUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class CameraListExecutor extends AbstractCameraExecutor {
    private CameraListExecutor(@NonNull final InternalVentaContext context) {
        super(context, "list", "prints the list of cameras");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        getConsole().header("Cameras:");

        final var cameraManager = getManagers().get(CameraManagerImplementation.class);
        StreamEx.of(cameraManager.iterator())
                .map(AbstractView::getPublicInformation)
                .map(FormatUtil::indent)
                .forEach(getConsole()::info);
    }
}
