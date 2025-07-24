package com.venta.engine.model.view;

import java.util.HashMap;
import java.util.Map;

import com.venta.engine.enums.TextureType;
import com.venta.engine.managers.MaterialManager;
import com.venta.engine.renderers.AbstractRenderer;

public final class MaterialView extends AbstractRenderer.AbstractView<MaterialManager.MaterialEntity> {
    private final Map<TextureType, TextureView> textures = new HashMap<>();
    
    public MaterialView(final String id, final MaterialManager.MaterialEntity entity) {
        super(id, entity);
    }
    
    public void setTexture(final TextureType type, final TextureView texture) {
        textures.put(type, texture);
    }

    public TextureView getTexture(final TextureType texture) {
        return textures.get(texture);
    }
}
