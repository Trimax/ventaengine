package io.github.trimax.venta.engine.executors.mesh;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.registries.implementation.MeshRegistryImplementation;
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
        final var meshRegistry = getRegistry(MeshRegistryImplementation.class);
        if (command.asArgument().isBlank()) {
            getConsole().warning("Usage: %s <id>", command.getFullPath());
            return;
        }

        try {
            final var mesh = meshRegistry.get(command.asArgument().value());
            if (mesh == null) {
                getConsole().error("Mesh <%s> can't be described because it does not exist", command.asArgument().value());
                return;
            }

            getConsole().header("Mesh <%s>:", mesh.getID());
            getConsole().info("     Vertices: %s", mesh.getVerticesCount() / Definitions.COUNT_FLOATS_PER_VERTEX);
            getConsole().info("       Facets: %s", mesh.getFacetsCount() / Definitions.COUNT_VERTICES_PER_FACET);
            getConsole().info("        Edges: %s", mesh.getEdgesCount() / Definitions.COUNT_VERTICES_PER_EDGE);
        } catch (final RuntimeException e) {
            getConsole().error("Mesh <%s> can't be described because it does not exist", command.asArgument().value());
        }
    }
}
