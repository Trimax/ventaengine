package io.github.trimax.examples.gridmesh.water.state;

import io.github.trimax.venta.engine.model.instance.CameraInstance;
import lombok.Data;

@Data
public final class WaterApplicationState {
    private float cameraAngle = (float) Math.PI / 4;
    private float cameraDistance = 2.5f;
    private CameraInstance camera;
}
