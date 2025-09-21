package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.engine.model.common.dto.Frame;

import java.util.List;

public record SpriteDTO(String texture, List<Frame> frames, float duration, boolean looping) {
}
