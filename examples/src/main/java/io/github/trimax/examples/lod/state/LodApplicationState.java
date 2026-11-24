package io.github.trimax.examples.lod.state;

import java.util.ArrayList;
import java.util.List;

import io.github.trimax.venta.engine.model.instance.CameraInstance;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import lombok.Data;

@Data
public final class LodApplicationState {
    private final List<ObjectInstance> spheres = new ArrayList<>();

    private CameraInstance camera;

    /** The seconds since the levels were counted the last time */
    private float sinceReport;
}
