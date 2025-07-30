package io.github.trimax.venta.engine.adapters;

import io.github.trimax.venta.engine.enums.TextureType;

public final class TextureTypeAdapter extends AbstractAdapter<TextureType> {
    public TextureTypeAdapter() {
        super(TextureType.class, TextureType::getFieldName);
    }
}