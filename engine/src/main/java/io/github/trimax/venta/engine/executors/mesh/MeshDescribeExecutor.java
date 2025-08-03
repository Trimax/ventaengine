package io.github.trimax.venta.engine.executors.mesh;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleQueue;
import io.github.trimax.venta.engine.context.InternalVentaContext;
import io.github.trimax.venta.engine.managers.implementation.MeshManagerImplementation;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class MeshDescribeExecutor extends AbstractMeshExecutor {
    private MeshDescribeExecutor(@NonNull final InternalVentaContext context) {
        super(context, "describe", "prints information about the mesh");
    }

    @Override
    public void execute(final ConsoleQueue.Command command) {
        final var meshManager = getManagers().get(MeshManagerImplementation.class);
        if (command.asArgument().isBlank()) {
            getConsole().warning("Usage: %s <id>", command.getFullPath());
            return;
        }

        final var mesh = meshManager.getEntity(command.asArgument().value());
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
