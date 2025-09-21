package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.enums.TextureFormat;
import io.github.trimax.venta.engine.model.entity.TextureEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.nio.ByteBuffer;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class TextureEntityImplementation extends AbstractEntityImplementation implements TextureEntity {
    @NonNull
    ByteBuffer buffer;

    @NonNull
    TextureFormat format;

    int internalID;
    int width;
    int height;
}
