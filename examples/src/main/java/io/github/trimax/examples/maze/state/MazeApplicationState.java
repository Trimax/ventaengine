package io.github.trimax.examples.maze.state;

import io.github.trimax.venta.engine.model.instance.CameraInstance;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import lombok.Data;

@Data
public final class MazeApplicationState {
    private CameraInstance camera;
    private LightInstance light;
}
