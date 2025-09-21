package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.model.entity.SoundEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class SoundEntityImplementation extends AbstractEntityImplementation implements SoundEntity {
    int bufferID;
    float duration;
}
