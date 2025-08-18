package io.github.trimax.examples.debug.handlers;

import io.github.trimax.examples.debug.state.DebugApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;
import org.joml.Vector3f;

@RequiredArgsConstructor
public final class DebugApplicationUpdateHandler implements VentaEngineUpdateHandler {
    private final DebugApplicationState state;

    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        state.getTetrahedron().rotate(new Vector3f(0.1f * (float) time.getDelta(), 0.2f * (float) time.getDelta(), 0.3f * (float)  time.getDelta()));
        state.getTetrahedron().setScale(new Vector3f(1.f, 1.f + 0.5f * (float) Math.sin(time.getTimeElapsed()), 1.f));
    }
}
