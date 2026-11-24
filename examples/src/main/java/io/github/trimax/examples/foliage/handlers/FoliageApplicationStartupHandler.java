package io.github.trimax.examples.foliage.handlers;

import org.joml.Vector2f;
import org.joml.Vector3f;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.common.shared.DirectionalLight;
import io.github.trimax.venta.engine.model.common.shared.Wind;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Cutout materials: trees made of crossed cards of leaves, a chain link fence and two panes of glass. The leaves and the
 * fence are holes where the texture is transparent (they need no sorting and their shadows have holes too), the glass
 * mixes colors and is drawn from the farthest pane to the nearest.
 */
@Slf4j
@AllArgsConstructor
public final class FoliageApplicationStartupHandler implements VentaEngineStartupHandler {
    private static final float QUARTER_TURN = (float) (Math.PI / 2.0);
    private static final float[][] TREES = {{-14.f, -4.f, 7.f}, {-6.f, -10.f, 9.f}, {4.f, -6.f, 8.f}, {13.f, -9.f, 6.5f}, {-1.f, -17.f, 10.f}};
    private static final int CARDS = 6;

    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        log.info("Foliage demo application started");

        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.8f));
        scene.setSkybox(context.getCubemapRegistry().get("clouds.json"));

        // The leaves follow the wind (see leaves.json): the cards sway, the trunks and the shadows too
        scene.setWind(new Wind(1.f, 0.3f, 0.5f, 1.2f));

        // The sun is low, so the shadows of the leaves lie long on the ground and show their holes
        final var sun = new DirectionalLight(new Vector3f(1.f, 0.93f, 0.8f), new Vector3f(-0.6f, -0.55f, -0.35f), 1.6f);
        sun.setCastShadows(true);
        sun.setShadowDistance(120.f);
        scene.setDirectionalLight(sun);

        final var plane = context.getObjectRepository().get("plane.json");
        final var cube = context.getObjectRepository().get("cube.json");
        final var grass = context.getMaterialRegistry().get("grass.json");
        grass.setTiling(new Vector2f(40.f, 40.f));
        final var leaves = context.getMaterialRegistry().get("leaves.json");
        final var fence = context.getMaterialRegistry().get("fence.json");
        final var glass = context.getMaterialRegistry().get("glass.json");
        final var bark = context.getMaterialRegistry().get("rust.json");

        final var ground = context.getObjectManager().create("Ground", plane);
        ground.setMaterial(grass);
        ground.setScale(new Vector3f(90.f, 1.f, 90.f));
        scene.add(ground);

        for (int tree = 0; tree < TREES.length; tree++) {
            final var x = TREES[tree][0];
            final var z = TREES[tree][1];
            final var height = TREES[tree][2];

            final var trunk = context.getObjectManager().create("Trunk " + tree, cube);
            trunk.setMaterial(bark);
            trunk.setScale(new Vector3f(0.5f, height * 0.6f, 0.5f));
            trunk.setPosition(new Vector3f(x, height * 0.3f, z));
            scene.add(trunk);

            // A vertical card has its plane turned around the z axis a quarter, then it is turned around the vertical axis
            for (int card = 0; card < CARDS; card++) {
                final var leaf = context.getObjectManager().create("Leaves " + tree + "-" + card, plane);
                leaf.setMaterial(leaves);
                leaf.setScale(new Vector3f(height * 0.75f, 1.f, height * 0.45f));
                leaf.setRotation(new Vector3f(0.f, (float) (card * Math.PI / CARDS), QUARTER_TURN));
                leaf.setPosition(new Vector3f(x, height * 0.55f + height * 0.08f * (card % 3), z));
                scene.add(leaf);
            }
        }

        // The fence in front of the trees
        for (int panel = 0; panel < 6; panel++) {
            final var wire = context.getObjectManager().create("Fence " + panel, plane);
            wire.setMaterial(fence);
            wire.setScale(new Vector3f(3.2f, 1.f, 2.4f));
            wire.setRotation(new Vector3f(QUARTER_TURN, 0.f, 0.f));
            wire.setPosition(new Vector3f(-8.f + panel * 3.2f, 1.2f, 3.f));
            scene.add(wire);
        }

        // Two panes: the near one is drawn after the far one whatever the order they were added in
        final var near = context.getObjectManager().create("Near glass", plane);
        near.setMaterial(glass);
        near.setScale(new Vector3f(3.f, 1.f, 3.f));
        near.setRotation(new Vector3f(QUARTER_TURN, 0.f, 0.f));
        near.setPosition(new Vector3f(3.f, 1.6f, 8.f));
        scene.add(near);

        final var far = context.getObjectManager().create("Far glass", plane);
        far.setMaterial(glass);
        far.setScale(new Vector3f(3.f, 1.f, 3.f));
        far.setRotation(new Vector3f(QUARTER_TURN, 0.f, 0.f));
        far.setPosition(new Vector3f(4.5f, 1.6f, 6.f));
        scene.add(far);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(0.f, 3.f, 17.f));
        camera.lookAt(new Vector3f(0.f, 3.f, 0.f));
    }
}
