package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.model.entity.TextureArrayEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class TextureArrayEntityImplementation extends AbstractEntityImplementation implements TextureArrayEntity {
    int internalID;
    int layersCount;
    int width;
    int height;
}
