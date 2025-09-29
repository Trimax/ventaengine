package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.SpriteEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpriteBinder extends AbstractBinder {
    private final TextureBinder textureBinder;

    public void bindSprite(final ProgramEntityImplementation program, final SpriteEntityImplementation sprite, final int frameIndex) {
        textureBinder.bind(TextureType.Diffuse, program, sprite.getTexture());

        bind(program.getUniformID(ShaderUniform.FrameIndex), frameIndex);
        bind(program.getUniformID(ShaderUniform.Color), sprite.getColor());
        bind(program.getUniformID(ShaderUniform.Frames), sprite.getFramesBuffer());
    }
}
