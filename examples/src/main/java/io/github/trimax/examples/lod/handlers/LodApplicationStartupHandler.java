package io.github.trimax.examples.lod.handlers;

import org.joml.Vector2f;
import org.joml.Vector3f;

import io.github.trimax.examples.lod.state.LodApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.common.shared.DirectionalLight;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A field of spheres: {@value #COLUMNS} in a row and {@value #ROWS} rows, one behind another. The far rows are small on the
 * screen, so their spheres are drawn with the simpler models and then with the cards.
 */
@Slf4j
@AllArgsConstructor
public final class LodApplicationStartupHandler implements VentaEngineStartupHandler {
    private static final int COLUMNS = 15;
    private static final int ROWS = 40;
    private static final float SPACING = 5.f;
    private static final float RADIUS = 1.2f;

    private final LodApplicationState state;

    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.8f));
        scene.setSkybox(context.getCubemapRegistry().get("clouds.json"));

        final var sun = new DirectionalLight(new Vector3f(1.f, 0.93f, 0.8f), new Vector3f(-0.6f, -0.55f, -0.35f), 1.6f);
        sun.setCastShadows(true);
        sun.setShadowDistance(150.f);
        scene.setDirectionalLight(sun);

        final var grass = context.getMaterialRegistry().get("grass.json");
        grass.setTiling(new Vector2f(120.f, 120.f));
        final var ground = context.getObjectManager().create("Ground", context.getObjectRepository().get("plane.json"));
        ground.setMaterial(grass);
        ground.setScale(new Vector3f(160.f, 1.f, 320.f));
        ground.setPosition(new Vector3f(0.f, 0.f, -110.f));
        scene.add(ground);

        final var sphere = context.getObjectRepository().get("lod/sphere.json");
        for (int row = 0; row < ROWS; row++)
            for (int column = 0; column < COLUMNS; column++) {
                final var object = context.getObjectManager().create("Sphere " + row + "-" + column, sphere);
                object.setScale(new Vector3f(2.f * RADIUS));
                object.setPosition(new Vector3f((column - (COLUMNS - 1) / 2.f) * SPACING, RADIUS, -row * SPACING));
                scene.add(object);
                state.getSpheres().add(object);
            }

        state.setCamera(context.getCameraManager().getCurrent());
        log.info("Levels of detail demo started: {} spheres", state.getSpheres().size());
    }
}
