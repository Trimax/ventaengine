package io.github.trimax.examples.spotlight.handlers;

import org.joml.Vector3f;

import io.github.trimax.examples.spotlight.state.SpotLightApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.common.shared.Attenuation;
import io.github.trimax.venta.engine.model.common.shared.Cone;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.prefabs.LightPrefab;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A ring of pillars and a few cubes on a plane without the sun: a white spot light hangs over the middle and looks down at
 * it, a warm and a cold one hang at the sides and sweep over the pillars.
 */
@Slf4j
@AllArgsConstructor
public final class SpotLightApplicationStartupHandler implements VentaEngineStartupHandler {
    private static final int PILLARS = 8;
    private static final float RING_RADIUS = 11.f;
    private static final float LIGHT_RANGE = 70.f;

    private final SpotLightApplicationState state;

    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Spot light demo application started");

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

        final var lamp = context.getLightRepository().get("spot.json");
        light(context, scene, lamp, "White light", new Vector3f(1.f, 0.95f, 0.85f), new Vector3f(0.f, 24.f, 0.f), new Cone(14.f, 24.f), false);
        light(context, scene, lamp, "Warm light", new Vector3f(1.f, 0.55f, 0.25f), new Vector3f(-18.f, 16.f, 14.f), new Cone(12.f, 22.f), true);
        light(context, scene, lamp, "Cold light", new Vector3f(0.3f, 0.5f, 1.f), new Vector3f(18.f, 16.f, 14.f), new Cone(12.f, 22.f), true);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(0.f, 22.f, 30.f));
        camera.lookAt(new Vector3f(0.f, 0.f, 0.f));
    }

    private void light(final VentaContext context, final SceneInstance scene, final LightPrefab prefab, final String name,
                       final Vector3f color, final Vector3f position, final Cone cone, final boolean sweeping) {
        final var light = context.getLightManager().create(name, prefab);
        light.setColor(color);
        light.setPosition(position);
        light.setCone(cone);
        light.setDirection(new Vector3f(position).negate());
        light.setIntensity(sweeping ? 450.f : 330.f);
        light.setAttenuation(new Attenuation(1.f, 0.f, 0.08f));
        light.setRange(LIGHT_RANGE);
        light.setCastShadows(true);
        scene.add(light);

        if (sweeping)
            state.getSweeping().add(light);
    }
}
