package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.model.common.dto.Frame;
import io.github.trimax.venta.engine.model.entity.SpriteEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class SpriteEntityImplementation extends AbstractEntityImplementation implements SpriteEntity {
    private final TextureEntityImplementation texture;
    private final List<Frame> frames;
    private final boolean looping;
    private final float duration;
}
