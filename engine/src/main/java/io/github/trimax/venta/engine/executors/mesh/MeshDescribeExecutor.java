package io.github.trimax.venta.engine.executors.mesh;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.managers.implementation.MeshManagerImplementation;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class MeshDescribeExecutor extends AbstractMeshExecutor {
    private MeshDescribeExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "describe", "prints information about the mesh");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        final var meshManager = getManager(MeshManagerImplementation.class);
        if (command.asArgument().isBlank()) {
            getConsole().warning("Usage: %s <id>", command.getFullPath());
            return;
        }

        final var mesh = meshManager.getInstance(command.asArgument().value());
        if (mesh == null) {
            getConsole().error("Mesh <%s> can't be described because it does not exist", command.asArgument().value());
            return;
        }

        getConsole().header("Mesh <%s>:", mesh.getID());
        getConsole().info("         Name: %s", mesh.getName());
        getConsole().info("  Material ID: %s", mesh.hasMaterial() ? mesh.getMaterial().getID() : "not assigned");
        getConsole().info("     Vertices: %s", mesh.getVerticesCount());
        getConsole().info("       Facets: %s", mesh.getFacetsCount());
        getConsole().info("        Edges: %s", mesh.getEdgesCount());
    }
}
