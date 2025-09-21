package io.github.trimax.examples.sound.state;

import io.github.trimax.venta.engine.model.instance.SoundSourceInstance;
import lombok.Data;

@Data
public final class SoundApplicationState {
    private SoundSourceInstance engine;
}
