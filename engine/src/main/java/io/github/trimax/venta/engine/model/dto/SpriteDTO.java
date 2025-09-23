package io.github.trimax.venta.engine.model.dto;

import java.util.List;

import io.github.trimax.venta.engine.model.common.dto.Frame;
import lombok.NonNull;

public record SpriteDTO(@NonNull String texture,
                        @NonNull List<Frame> frames,
                        float duration,
                        boolean looping) {
}
