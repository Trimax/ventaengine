package io.github.trimax.examples.pointshadow.handlers;

import org.joml.Vector3f;

import io.github.trimax.examples.pointshadow.state.PointShadowApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.common.shared.Attenuation;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.prefabs.LightPrefab;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A ring of pillars and a few cubes on a plane without the sun: two warm lights fly around, a cold one stands among the pillars.
 */
@Slf4j
@AllArgsConstructor
public final class PointShadowApplicationStartupHandler implements VentaEngineStartupHandler {
    private static final int PILLARS = 8;
    private static final float RING_RADIUS = 11.f;
    private static final float LIGHT_RANGE = 45.f;

    private final PointShadowApplicationState state;

    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Point light shadow demo application started");

        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.03f));

        final var plane = context.getObjectManager().create("plane", context.getObjectRepository().get("plane.json"));
        plane.setScale(new Vector3f(100.f));
        scene.add(plane);

        final var cube = context.getObjectRepository().get("cube.json");
        for (int i = 0; i < PILLARS; i++) {
            final var angle = (float) (2.0 * Math.PI * i / PILLARS);
            final var pillar = context.getObjectManager().create("Pillar " + i, cube);
            pillar.setScale(new Vector3f(1.6f, 7.f, 1.6f));
            pillar.setPosition(new Vector3f(RING_RADIUS * (float) Math.cos(angle), 3.5f, RING_RADIUS * (float) Math.sin(angle)));
            scene.add(pillar);
        }

        for (int i = 0; i < 5; i++) {
            final var block = context.getObjectManager().create("Block " + i, cube);
            block.setScale(new Vector3f(1.f + i * 0.4f));
            block.setPosition(new Vector3f(-4.f + i * 2.f, 0.5f + i * 0.2f, (i % 2 == 0 ? 1.f : -1.f) * 2.f));
            scene.add(block);
        }

        final var lantern = context.getLightRepository().get("point.json");
        light(context, scene, lantern, "Warm light", new Vector3f(1.f, 0.65f, 0.35f), new Vector3f(6.f, 4.f, 0.f), true);
        light(context, scene, lantern, "Red light", new Vector3f(1.f, 0.2f, 0.15f), new Vector3f(0.f, 3.f, 6.f), true);
        light(context, scene, lantern, "Cold light", new Vector3f(0.3f, 0.5f, 1.f), new Vector3f(-RING_RADIUS + 3.f, 5.f, 4.f), false);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(0.f, 20.f, 28.f));
        camera.lookAt(new Vector3f(0.f, 0.f, 0.f));
    }

    private void light(final VentaContext context, final SceneInstance scene,
                       final LightPrefab prefab, final String name, final Vector3f color,
                       final Vector3f position, final boolean orbiting) {
        final var light = context.getLightManager().create(name, prefab);
        light.setColor(color);
        light.setPosition(position);
        light.setIntensity(45.f);
        light.setAttenuation(new Attenuation(1.f, 0.f, 0.08f));
        light.setRange(LIGHT_RANGE);
        light.setCastShadows(true);
        scene.add(light);

        if (orbiting)
            state.getOrbiting().add(light);
    }
}
