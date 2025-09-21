package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.model.common.dto.Frame;
import io.github.trimax.venta.engine.model.entity.SpriteEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class SpriteEntityImplementation extends AbstractEntityImplementation implements SpriteEntity {
    @NonNull
    TextureEntityImplementation texture;

    @NonNull
    List<Frame> frames;

    boolean looping;
    float duration;
}
