package io.github.trimax.venta.engine.executors.material;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.registries.implementation.MaterialRegistryImplementation;
import io.github.trimax.venta.engine.utils.FormatUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class MaterialListExecutor extends AbstractMaterialExecutor {
    private MaterialListExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "list", "prints the list of materials");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        getConsole().header("Materials:");

        final var meshRegistry = getRegistry(MaterialRegistryImplementation.class);
        StreamEx.of(meshRegistry.getIds())
                .map(FormatUtil::indent)
                .forEach(getConsole()::info);
    }
}
