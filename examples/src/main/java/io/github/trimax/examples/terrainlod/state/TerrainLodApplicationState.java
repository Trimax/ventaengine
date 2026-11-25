package io.github.trimax.examples.terrainlod.state;

import io.github.trimax.venta.engine.model.instance.CameraInstance;
import io.github.trimax.venta.engine.model.instance.TerrainSurfaceInstance;
import lombok.Data;

@Data
public final class TerrainLodApplicationState {
    private CameraInstance camera;
    private TerrainSurfaceInstance terrain;

    /** The height the camera flies at over the ground, smoothed, so it doesn't jerk over the ridges */
    private float altitude = Float.NaN;

    /** Whether the terrain is drawn as lines: the levels of detail show in the density of them */
    private boolean wireframe = true;

    /** Whether the key that switches the drawing was pushed in the last frame, so a hold doesn't switch it again and again */
    private boolean switchPushed;
}
