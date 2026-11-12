package io.github.trimax.examples.scene.handlers;

import org.joml.Vector2f;
import org.joml.Vector3f;

import io.github.trimax.examples.scene.state.SceneApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.common.shared.Attenuation;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.instance.TerrainSurfaceInstance;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A castle on a hill by a lake at sunset: the sky, the sun and the fog come from the scene file,
 * the rest is placed on the terrain with its height.
 */
@Slf4j
@AllArgsConstructor
public final class SceneApplicationStartupHandler implements VentaEngineStartupHandler {
    /* Positions on the terrain (x, z), see textures/heightmaps/valley.png */
    private static final Vector2f CASTLE = new Vector2f(40.f, -32.f);
    private static final Vector2f CAMPFIRE = new Vector2f(3.f, 1.5f);

    private static final float WATER_LEVEL = -10.35f;

    /* The castle mesh is normalized to a unit cube around the origin: its base is 0.45 of its size below the center */
    private static final float CASTLE_SIZE = 45.f;
    private static final float CASTLE_BASE = 0.45f * CASTLE_SIZE - 1.5f;

    private final SceneApplicationState state;

    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Castle by the lake demo started");

        final var scene = context.getSceneManager().create("Castle by the lake", context.getSceneRepository().get("demo.json"));
        context.getSceneManager().setCurrent(scene);

        final var terrain = context.getTerrainSurfaceManager().create("Valley", context.getTerrainSurfaceRepository().get("valley.json"));
        scene.add(terrain);
        state.setTerrain(terrain);

        final var lake = context.getWaterSurfaceManager().create("Lake", context.getWaterSurfaceRepository().get("lake.json"));
        lake.setPosition(new Vector3f(0.f, WATER_LEVEL, 0.f));
        scene.add(lake);

        final var castle = context.getObjectManager().create("Castle", context.getObjectRepository().get("castle.json"));
        castle.setScale(new Vector3f(CASTLE_SIZE));
        castle.setPosition(onGround(terrain, CASTLE, CASTLE_BASE));
        scene.add(castle);

        createCampfire(context, scene, terrain);

        state.getCameraTarget().set(onGround(terrain, CASTLE, CASTLE_SIZE * 0.3f));
        state.setCamera(context.getCameraManager().getCurrent());
    }

    private void createCampfire(final VentaContext context, final SceneInstance scene, final TerrainSurfaceInstance terrain) {
        final var flame = context.getEmitterManager().create("Campfire flame", context.getEmitterRepository().get("campfire-flame.json"));
        flame.setPosition(onGround(terrain, CAMPFIRE, 0.3f));
        scene.add(flame);

        final var smoke = context.getEmitterManager().create("Campfire smoke", context.getEmitterRepository().get("campfire-smoke.json"));
        smoke.setPosition(onGround(terrain, CAMPFIRE, 2.5f));
        scene.add(smoke);

        final var light = context.getLightManager().create("Campfire light", context.getLightRepository().get("point.json"));
        light.setPosition(onGround(terrain, CAMPFIRE, 3.f));
        light.setColor(new Vector3f(1.f, 0.55f, 0.2f));
        light.setAttenuation(new Attenuation(1.f, 0.05f, 0.02f));
        scene.add(light);
        state.setCampfireLight(light);

        final var sound = context.getSoundSourceManager().create("Campfire sound", context.getSoundSourceRepository().get("campfire.json"));
        sound.setPosition(onGround(terrain, CAMPFIRE, 1.f));
        sound.play();
        scene.add(sound);
    }

    private static Vector3f onGround(final TerrainSurfaceInstance terrain, final Vector2f point, final float elevation) {
        return new Vector3f(point.x, terrain.getHeight(point.x, point.y) + elevation, point.y);
    }
}
