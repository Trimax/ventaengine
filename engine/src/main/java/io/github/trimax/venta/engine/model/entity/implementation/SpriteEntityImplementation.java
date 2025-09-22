package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.model.entity.SpriteEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.joml.Vector4f;

import java.nio.FloatBuffer;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class SpriteEntityImplementation extends AbstractEntityImplementation implements SpriteEntity {
    @NonNull
    TextureEntityImplementation texture;

    @NonNull
    FloatBuffer framesBuffer;

    @NonNull
    Vector4f color;

    boolean looping;
    int frameCount;
    float duration;
}
