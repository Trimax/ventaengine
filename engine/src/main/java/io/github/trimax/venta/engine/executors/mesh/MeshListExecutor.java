package io.github.trimax.venta.engine.executors.mesh;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.model.prefabs.AbstractPrefab;
import io.github.trimax.venta.engine.repositories.implementation.MeshRepositoryImplementation;
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

        final var meshRepository = getRepository(MeshRepositoryImplementation.class);
        StreamEx.of(meshRepository.iterator())
                .map(AbstractPrefab::getID)
                .map(FormatUtil::indent)
                .forEach(getConsole()::info);
    }
}
