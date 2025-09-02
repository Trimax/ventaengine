package io.github.trimax.examples.metalicity.state;

import io.github.trimax.venta.engine.model.instance.CameraInstance;
import lombok.Data;

@Data
public final class MetalicityApplicationState {
    private float cameraDistance = 2.5f;
    private float cameraAngle = 0.f;
    private CameraInstance camera;
}
