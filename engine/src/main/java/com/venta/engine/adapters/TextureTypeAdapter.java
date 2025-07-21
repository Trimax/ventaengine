package com.venta.engine.adapters;

import com.venta.engine.enums.TextureType;

public final class TextureTypeAdapter extends AbstractAdapter<TextureType> {
    public TextureTypeAdapter() {
        super(TextureType.class, TextureType::getFieldName);
    }
}