package io.github.trimax.examples.light.dynamic.state;

import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import lombok.Data;
import org.joml.Vector3f;

@Data
public final class DynamicLightApplicationState {
    private final Vector3f cubeRotationVelocity = new Vector3f(0, 0, 0);
    private ObjectInstance cube;
    private LightInstance lightXZ;
    private LightInstance lightXY;
    private LightInstance lightYZ;
}
