package io.github.trimax.examples.terrainlod.handlers;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;

import org.joml.Vector3f;

import io.github.trimax.examples.terrainlod.state.TerrainLodApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;

/**
 * The camera flies a long loop over the terrain, a little above the ground, and looks ahead along it: the terrain under it is
 * drawn finely and what lies ahead coarser and coarser.
 */
@RequiredArgsConstructor
public final class TerrainLodApplicationUpdateHandler implements VentaEngineUpdateHandler {
    /** The loop is an ellipse around the middle of the terrain: how far along the axes the camera goes, and how fast it goes round */
    private static final float RADIUS_X = 1500.f;
    private static final float RADIUS_Z = 1100.f;
    private static final float SPEED = 0.045f;

    /** How high over the ground the camera flies, and how fast the height follows the ground */
    private static final float CLEARANCE = 170.f;
    private static final float FOLLOW = 1.5f;

    /** How far ahead (in seconds of the flight) the camera looks */
    private static final float LOOK_AHEAD = 1.5f;

    private final TerrainLodApplicationState state;

    private final Vector3f position = new Vector3f();
    private final Vector3f target = new Vector3f();

    @Override
    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        switchDrawing(context);

        final var elapsed = (float) time.getTimeElapsed();
        final var terrain = state.getTerrain();

        final var ground = terrain.getHeight(getX(elapsed), getZ(elapsed));
        state.setAltitude(Float.isNaN(state.getAltitude()) ? ground + CLEARANCE
                : state.getAltitude() + (ground + CLEARANCE - state.getAltitude()) * Math.min(1.f, FOLLOW * (float) time.getDelta()));

        position.set(getX(elapsed), state.getAltitude(), getZ(elapsed));

        final var ahead = elapsed + LOOK_AHEAD / SPEED * 0.12f;
        target.set(getX(ahead), terrain.getHeight(getX(ahead), getZ(ahead)), getZ(ahead));

        state.getCamera().setPosition(position);
        state.getCamera().lookAt(target);
    }

    /** The space key switches between the lines and the solid terrain, once for a push */
    private void switchDrawing(final VentaContext context) {
        final var pushed = context.isButtonPushed(GLFW_KEY_SPACE);
        if (pushed && !state.isSwitchPushed()) {
            state.setWireframe(!state.isWireframe());
            state.getTerrain().setDrawMode(state.isWireframe() ? DrawMode.Edge : DrawMode.Polygon);
        }

        state.setSwitchPushed(pushed);
    }

    private static float getX(final float elapsed) {
        return RADIUS_X * (float) Math.cos(elapsed * SPEED);
    }

    private static float getZ(final float elapsed) {
        return RADIUS_Z * (float) Math.sin(elapsed * SPEED * 1.3f);
    }
}
