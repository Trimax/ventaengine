package io.github.trimax.venta.engine.executors.mesh;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.model.entity.AbstractEntity;
import io.github.trimax.venta.engine.registries.implementation.MeshRegistryImplementation;
import io.github.trimax.venta.engine.utils.FormatUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class MeshListExecutor extends AbstractMeshExecutor {
    private MeshListExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "list", "prints the list of meshes");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        getConsole().header("Meshes:");

        final var meshRegistry = getRegistry(MeshRegistryImplementation.class);
        StreamEx.of(meshRegistry.iterator())
                .map(AbstractEntity::getID)
                .map(FormatUtil::indent)
                .forEach(getConsole()::info);
    }
}
