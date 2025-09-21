package io.github.trimax.examples.sound.handlers;

import io.github.trimax.examples.sound.state.SoundApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;
import org.joml.Vector3f;

@RequiredArgsConstructor
public final class SoundApplicationUpdateHandler implements VentaEngineUpdateHandler {
    private final SoundApplicationState state;

    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        state.getEngine().setPosition(
                new Vector3f(2.5f * (float) Math.sin(time.getTimeElapsed()), 0.f, 2.5f * (float) Math.cos(time.getTimeElapsed())));
    }
}
