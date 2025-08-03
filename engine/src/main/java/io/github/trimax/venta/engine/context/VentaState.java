package io.github.trimax.venta.engine.context;

import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import lombok.Data;

@Data
public final class VentaState {
    private VentaEngineApplication application;
    private boolean isApplicationRunning = true;
    private boolean isDebugEnabled = false;
}
