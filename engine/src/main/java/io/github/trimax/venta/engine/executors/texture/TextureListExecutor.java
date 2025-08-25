package io.github.trimax.venta.engine.executors.texture;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.registries.implementation.TextureRegistryImplementation;
import io.github.trimax.venta.engine.utils.FormatUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class TextureListExecutor extends AbstractTextureExecutor {
    private TextureListExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "list", "prints the list of textures");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        getConsole().header("Textures:");

        final var textureRegistry = getRegistry(TextureRegistryImplementation.class);
        StreamEx.of(textureRegistry.getIds())
                .map(resourceName -> String.format("%s: %s", resourceName, textureRegistry.get(resourceName).getID()))
                .map(FormatUtil::indent)
                .forEach(getConsole()::info);
    }
}
