package io.github.trimax.examples.scene.state;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.instance.CameraInstance;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.instance.TerrainSurfaceInstance;
import lombok.Data;

@Data
public final class SceneApplicationState {
    /* The middle of the castle (see scenes/demo.json) */
    private final Vector3f cameraTarget = new Vector3f(40.f, 22.5f, -32.f);

    private float cameraAngle = 0.f;
    private float cameraDistance = 140.f;

    private TerrainSurfaceInstance terrain;
    private LightInstance campfireLight;
    private CameraInstance camera;
}
