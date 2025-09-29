package io.github.trimax.venta.engine.model.dto;

import java.util.List;

import io.github.trimax.venta.engine.model.dto.common.ColorDTO;
import io.github.trimax.venta.engine.model.dto.common.FrameDTO;
import lombok.NonNull;

public record SpriteDTO(@NonNull String texture,
                        @NonNull List<FrameDTO> frames,
                        ColorDTO color,
                        float duration,
                        boolean looping) {
}
