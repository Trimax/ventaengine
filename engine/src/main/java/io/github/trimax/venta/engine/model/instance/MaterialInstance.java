package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.entity.TextureEntity;

public interface MaterialInstance extends AbstractInstance {
    void setTexture(final TextureType type, final TextureEntity texture);

    TextureEntity getTexture(final TextureType texture);
}
