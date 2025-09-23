package io.github.trimax.venta.engine.utils;

import java.util.List;

import io.github.trimax.venta.engine.model.common.geo.Wave;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import one.util.streamex.StreamEx;

@UtilityClass
public final class WaveUtil {
    public float getAmplitude(@NonNull final List<Wave> waves) {
        return (float) StreamEx.of(waves).mapToDouble(Wave::amplitude).sum();
    }
}
