package io.github.trimax.venta.engine.model.common.scene;

import io.github.trimax.venta.engine.model.entity.TextureEntity;

public record Skybox(TextureEntity top,
                     TextureEntity bottom,
                     TextureEntity left,
                     TextureEntity right,
                     TextureEntity front,
                     TextureEntity back) {
}
