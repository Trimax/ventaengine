package io.github.trimax.examples.camera.state;

import io.github.trimax.venta.engine.model.instance.CameraInstance;
import lombok.Data;

@Data
public final class CameraApplicationState {
    private float cameraAngle = 0.f;
    private float cameraDistance = 2.5f;
    private CameraInstance camera;
}
