package io.github.trimax.venta.engine.executors.light;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.managers.implementation.LightManagerImplementation;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class LightDescribeExecutor extends AbstractLightExecutor {
    private LightDescribeExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "describe", "prints information about the light");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        final var lightManager = getManager(LightManagerImplementation.class);
        if (command.asArgument().isBlank()) {
            getConsole().warning("Usage: %s <id>", command.getFullPath());
            return;
        }

        final var light = lightManager.getInstance(command.asArgument().value());
        if (light == null) {
            getConsole().error("Light <%s> can't be described because it does not exist", command.asArgument().value());
            return;
        }

        getConsole().header("Light <%s>:", light.getID());
        getConsole().info("          Name: %s", light.getName());
        getConsole().info("          Type: %s", light.getType().name());
        getConsole().info("      Position: %s", light.getPosition());
        getConsole().info("     Direction: %s", light.getDirection());
        getConsole().info("     Intensity: %.2f", light.getIntensity());
        getConsole().info("       Enabled: %b", light.isEnabled());
        getConsole().info("  Cast shadows: %b", light.isCastShadows());
        getConsole().info("   Attenuation: %s", light.getAttenuation());
        getConsole().info("         Color: %s", light.getColor());
    }
}
