package io.github.trimax.examples.scene.handlers;

import io.github.trimax.examples.scene.state.SceneApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.instance.SoundSourceInstance;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A castle on a hill by a lake at sunset: the whole scene is described in scenes/demo.json,
 * the code only keeps what the update handler animates.
 */
@Slf4j
@AllArgsConstructor
public final class SceneApplicationStartupHandler implements VentaEngineStartupHandler {
    private static final String CAMPFIRE_LIGHT = "Campfire light";

    private final SceneApplicationState state;

    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Castle by the lake demo started");

        final var scene = context.getSceneManager().create("Castle by the lake", context.getSceneRepository().get("demo.json"));
        context.getSceneManager().setCurrent(scene);
        scene.getSoundSources().forEach(SoundSourceInstance::play);

        state.setTerrain(scene.getTerrainSurfaces().iterator().next());
        state.setCampfireLight(scene.getLights().stream()
                .filter(light -> CAMPFIRE_LIGHT.equals(light.getName()))
                .findFirst()
                .orElseThrow());
        state.setCamera(context.getCameraManager().getCurrent());
    }
}
