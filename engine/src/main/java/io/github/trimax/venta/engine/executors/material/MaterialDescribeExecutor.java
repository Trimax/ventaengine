package io.github.trimax.venta.engine.executors.material;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.registries.implementation.MaterialRegistryImplementation;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class MaterialDescribeExecutor extends AbstractMaterialExecutor {
    private MaterialDescribeExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "describe", "prints information about the material");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        final var materialRegistry = getRegistry(MaterialRegistryImplementation.class);
        if (command.asArgument().isBlank()) {
            getConsole().warning("Usage: %s <id>", command.getFullPath());
            return;
        }

        try {
            final var material = materialRegistry.get(command.asArgument().value());
            if (material == null) {
                getConsole().error("Material <%s> can't be described because it does not exist", command.asArgument().value());
                return;
            }

            getConsole().header("Material <%s>:", command.asArgument().value());
            getConsole().info("       Metalness: %.2f", material.getMetalness());
            getConsole().info("       Roughness: %.2f", material.getRoughness());
            getConsole().info("          Tiling: %s", material.getTiling());
            getConsole().info("          Offset: %s", material.getOffset());
            getConsole().info("           Color: %s", material.getColor());
            getConsole().info("  Textures count: %d", material.getTextures().size());
            material.getTextures().forEach((type, texture) ->
                    getConsole().info("   Texture %s: %s", texture.getID(), type));
        } catch (final RuntimeException e) {
            getConsole().error("Material <%s> can't be described because it does not exist", command.asArgument().value());
        }
    }
}
