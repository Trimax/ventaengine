package io.github.trimax.examples.maze.state;

import io.github.trimax.venta.engine.model.instance.CameraInstance;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
public final class MazeApplicationState {
    private final Set<Integer> pushedButtons = new HashSet<>();
    private CameraInstance camera;
    private LightInstance light;
}
