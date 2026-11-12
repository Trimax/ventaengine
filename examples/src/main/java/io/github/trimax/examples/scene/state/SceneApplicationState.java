package io.github.trimax.examples.scene.state;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.instance.CameraInstance;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.instance.TerrainSurfaceInstance;
import lombok.Data;

@Data
public final class SceneApplicationState {
    private final Vector3f cameraTarget = new Vector3f();

    private float cameraAngle = 0.f;
    private float cameraDistance = 140.f;

    private TerrainSurfaceInstance terrain;
    private LightInstance campfireLight;
    private CameraInstance camera;
}
