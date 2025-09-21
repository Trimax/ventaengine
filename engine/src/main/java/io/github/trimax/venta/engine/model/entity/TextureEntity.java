package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.enums.TextureFormat;

public interface TextureEntity extends AbstractEntity {
    int getWidth();

    int getHeight();

    TextureFormat getFormat();
}
