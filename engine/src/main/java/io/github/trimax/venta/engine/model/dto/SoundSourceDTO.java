package io.github.trimax.venta.engine.model.dto;

public record SoundSourceDTO(String sound,
                             Float volume,
                             Float pitch,
                             Boolean looping) {
}