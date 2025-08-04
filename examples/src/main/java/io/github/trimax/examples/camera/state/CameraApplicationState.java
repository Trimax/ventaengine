package io.github.trimax.examples.camera.state;

import java.util.HashSet;
import java.util.Set;

import io.github.trimax.venta.engine.model.view.CameraView;
import io.github.trimax.venta.engine.model.view.ObjectView;
import lombok.Data;

@Data
public final class CameraApplicationState {
    private final Set<Integer> pushedButtons = new HashSet<>();
    private float cameraAngle = 0.f;
    private float cameraDistance = 2.5f;
    private CameraView camera;
    private ObjectView cube;
}
