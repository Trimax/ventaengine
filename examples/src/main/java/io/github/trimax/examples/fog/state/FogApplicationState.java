package io.github.trimax.examples.fog.state;

import io.github.trimax.venta.engine.model.common.scene.Fog;
import io.github.trimax.venta.engine.model.instance.CameraInstance;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public final class FogApplicationState {
    private final List<ObjectInstance> cubes = new ArrayList<>();
    private CameraInstance camera;
    private Fog fog;
}
