package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.model.entity.SoundEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.ShortBuffer;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class SoundEntityImplementation extends AbstractEntityImplementation implements SoundEntity {
    private final ShortBuffer buffer;
    private float duration;
}
