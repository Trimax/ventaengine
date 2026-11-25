package io.github.trimax.examples.pointshadow.state;

import java.util.ArrayList;
import java.util.List;

import io.github.trimax.venta.engine.model.instance.LightInstance;
import lombok.Data;

@Data
public final class PointShadowApplicationState {
    /** The lights that fly around the pillars */
    private final List<LightInstance> orbiting = new ArrayList<>();
}
