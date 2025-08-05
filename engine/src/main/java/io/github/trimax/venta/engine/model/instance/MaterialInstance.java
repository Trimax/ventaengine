package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.enums.TextureType;

public interface MaterialInstance extends AbstractInstance {
    void setTexture(final TextureType type, final TextureInstance texture);

    TextureInstance getTexture(final TextureType texture);
}
