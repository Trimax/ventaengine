package io.github.trimax.venta.engine.model.dto;

import lombok.NonNull;

public record SoundSourceDTO(@NonNull String sound,
                             Float volume,
                             Float pitch,
                             Boolean looping,
                             Float referenceDistance,
                             Float maximalDistance,
                             Float rolloff) {
}