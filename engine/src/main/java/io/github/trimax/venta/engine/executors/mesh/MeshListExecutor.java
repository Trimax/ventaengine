package io.github.trimax.venta.engine.executors.mesh;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.managers.implementation.MeshManagerImplementation;
import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import io.github.trimax.venta.engine.utils.FormatUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class MeshListExecutor extends AbstractMeshExecutor {
    private MeshListExecutor(@NonNull final InternalVentaContext context) {
        super(context, "list", "prints the list of meshes");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        getConsole().header("Meshes:");

        final var meshManager = getManagers().get(MeshManagerImplementation.class);
        StreamEx.of(meshManager.iterator())
                .map(AbstractInstance::getPublicInformation)
                .map(FormatUtil::indent)
                .forEach(getConsole()::info);
    }
}
