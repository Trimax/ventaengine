package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElevationBinder extends AbstractBinder {
    private final TextureBinder textureBinder;

    public void bind(final ProgramEntityImplementation program, final TextureEntityImplementation heightmap, final float factor) {
        textureBinder.bind(TextureType.Height, program, heightmap);
        bind(program.getUniformID(ShaderUniform.Factor), factor);
    }
}
