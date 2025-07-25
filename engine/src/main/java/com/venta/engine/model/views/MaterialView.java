package com.venta.engine.model.views;

import com.venta.engine.enums.TextureType;

public interface MaterialView extends AbstractView {
    void setTexture(final TextureType type, final TextureView texture);

    TextureView getTexture(final TextureType texture);
}
