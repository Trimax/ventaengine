package com.venta.examples.cube.state;

import java.util.HashSet;
import java.util.Set;

import org.joml.Vector3f;

import com.venta.engine.model.view.LightView;
import com.venta.engine.model.view.ObjectView;
import lombok.Data;

@Data
public final class CubeApplicationState {
    private final Vector3f cubeRotationVelocity = new Vector3f(0, 0, 0);
    private final Set<Integer> pushedButtons = new HashSet<>();
    private ObjectView cube;
    private ObjectView gizmo;
    private LightView light;
}
