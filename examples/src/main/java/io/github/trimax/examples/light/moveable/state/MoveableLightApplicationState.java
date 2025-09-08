package io.github.trimax.examples.light.moveable.state;

import io.github.trimax.venta.engine.model.instance.LightInstance;
import lombok.Data;

@Data
public final class MoveableLightApplicationState {
    private LightInstance light;
}
