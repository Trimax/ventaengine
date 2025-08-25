package io.github.trimax.venta.engine.executors.texture;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.registries.implementation.TextureRegistryImplementation;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class TextureDescribeExecutor extends AbstractTextureExecutor {
    private TextureDescribeExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "describe", "prints information about the texture");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        final var textureRegistry = getRegistry(TextureRegistryImplementation.class);
        if (command.asArgument().isBlank()) {
            getConsole().warning("Usage: %s <id>", command.getFullPath());
            return;
        }

        try {
            final var texture = textureRegistry.get(command.asArgument().value());
            if (texture == null) {
                getConsole().error("Texture <%s> can't be described because it does not exist", command.asArgument().value());
                return;
            }

            getConsole().header("Texture <%s>:", command.asArgument().value());
            getConsole().info("            Size: %dx%d", texture.getWidth(), texture.getHeight());
            getConsole().info("          Format: %s", texture.getFormat().name());
            getConsole().info("  Channels count: %s", texture.getFormat().getChannelsCount());
        } catch (final RuntimeException e) {
            getConsole().error("Texture <%s> can't be described because it does not exist", command.asArgument().value());
        }
    }
}
