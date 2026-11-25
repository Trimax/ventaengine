package io.github.trimax.examples.spotlight.state;

import java.util.ArrayList;
import java.util.List;

import io.github.trimax.venta.engine.model.instance.LightInstance;
import lombok.Data;

@Data
public final class SpotLightApplicationState {
    /** The lights that sweep over the plane */
    private final List<LightInstance> sweeping = new ArrayList<>();
}
