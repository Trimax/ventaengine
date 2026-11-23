package io.github.trimax.examples.shadow.handlers;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.common.shared.DirectionalLight;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Cubes and tetrahedrons of different sizes on a plane under a low sun: the shadows of the sun fall on the plane and
 * on the other objects, the near ones are sharp and the far ones are coarser (cascades).
 */
@Slf4j
@AllArgsConstructor
public final class ShadowApplicationStartupHandler implements VentaEngineStartupHandler {
    private static final int ROWS = 5;
    private static final float SPACING = 12.f;

    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Shadow demo application started");

        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.2f));
        scene.setSkybox(context.getCubemapRegistry().get("clouds.json"));

        // The sun is low, so the shadows are long; the distance of the shadows covers the whole plane
        final var sun = new DirectionalLight(new Vector3f(1.f, 0.95f, 0.85f), new Vector3f(-1.f, -0.6f, -0.4f), 1.3f);
        sun.setCastShadows(true);
        sun.setShadowDistance(250.f);
        scene.setDirectionalLight(sun);

        final var plane = context.getObjectManager().create("plane", context.getObjectRepository().get("plane.json"));
        plane.setScale(new Vector3f(120.f));
        scene.add(plane);

        final var cube = context.getObjectRepository().get("cube.json");
        final var tetrahedron = context.getObjectRepository().get("tetrahedron.json");

        for (int row = 0; row < ROWS; row++)
            for (int column = 0; column < ROWS; column++) {
                final var size = 1.f + (row + column) % 3 * 1.5f;
                final var object = context.getObjectManager().create("Object " + row + "-" + column, (row + column) % 2 == 0 ? cube : tetrahedron);
                object.setScale(new Vector3f(size));
                object.setPosition(new Vector3f((column - ROWS / 2) * SPACING, size / 2.f, (row - ROWS / 2) * SPACING));
                scene.add(object);
            }

        // A tall pillar next to the camera: its shadow crosses the nearest cascade
        final var pillar = context.getObjectManager().create("Pillar", cube);
        pillar.setScale(new Vector3f(1.5f, 8.f, 1.5f));
        pillar.setPosition(new Vector3f(-8.f, 4.f, 18.f));
        scene.add(pillar);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(25.f, 14.f, 40.f));
        camera.lookAt(new Vector3f(0.f, 0.f, 0.f));
    }
}
