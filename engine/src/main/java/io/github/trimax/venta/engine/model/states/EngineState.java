package io.github.trimax.venta.engine.model.states;

import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class EngineState extends AbstractState {
    private final VentaEngineApplication application;
    private boolean isApplicationRunning = true;
    private boolean isDebugEnabled = false;
}
