package io.github.trimax.venta.engine.model.prefabs;

import io.github.trimax.venta.engine.model.entity.SpriteEntity;
import org.joml.Vector2fc;

public interface BillboardPrefab extends AbstractPrefab {
    SpriteEntity getSprite();

    Vector2fc getSize();
}
