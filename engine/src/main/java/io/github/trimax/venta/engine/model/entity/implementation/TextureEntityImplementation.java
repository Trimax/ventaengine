package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.model.entity.TextureEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.ByteBuffer;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class TextureEntityImplementation extends AbstractEntityImplementation implements TextureEntity {
    private final ByteBuffer buffer;
    private final int internalID;
    private final int width;
    private final int height;
}
