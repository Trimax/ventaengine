package io.github.trimax.examples.fog.state;

import java.util.ArrayList;
import java.util.List;

import io.github.trimax.venta.engine.model.common.effects.Fog;
import io.github.trimax.venta.engine.model.instance.CameraInstance;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import lombok.Data;

@Data
public final class FogApplicationState {
    private final List<ObjectInstance> cubes = new ArrayList<>();
    private CameraInstance camera;
    private Fog fog;
}
