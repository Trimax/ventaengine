package io.github.trimax.venta.engine.executors.scene;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.managers.implementation.SceneManagerImplementation;
import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import io.github.trimax.venta.engine.utils.FormatUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class SceneListExecutor extends AbstractSceneExecutor {
    private SceneListExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "list", "prints the list of scenes");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        getConsole().header("Scenes:");

        final var sceneManager = getManager(SceneManagerImplementation.class);
        StreamEx.of(sceneManager.iterator())
                .map(AbstractInstance::getPublicInformation)
                .map(FormatUtil::indent)
                .forEach(getConsole()::info);
    }
}
