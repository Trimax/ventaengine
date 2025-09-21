package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.model.entity.implementation.SpriteEntityImplementation;
import io.github.trimax.venta.engine.model.prefabs.BillboardPrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.joml.Vector2f;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class BillboardPrefabImplementation extends AbstractPrefabImplementation implements BillboardPrefab {
    @NonNull
    SpriteEntityImplementation sprite;

    @NonNull
    Vector2f size;
}
