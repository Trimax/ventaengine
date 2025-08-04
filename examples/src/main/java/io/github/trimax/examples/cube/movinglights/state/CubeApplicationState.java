package io.github.trimax.examples.cube.movinglights.state;

import java.util.HashSet;
import java.util.Set;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.view.LightView;
import io.github.trimax.venta.engine.model.view.ObjectView;
import lombok.Data;

@Data
public final class CubeApplicationState {
    private final Vector3f cubeRotationVelocity = new Vector3f(0, 0, 0);
    private final Set<Integer> pushedButtons = new HashSet<>();
    private ObjectView cube;
    private LightView lightXZ;
    private LightView lightXY;
    private LightView lightYZ;
}
