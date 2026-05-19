package io.github.trimax.venta.engine.binders;

import java.util.Map;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureArrayEntityImplementation;
import io.github.trimax.venta.engine.registries.implementation.TextureArrayRegistryImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextureArrayBinder extends AbstractBinder {
    private final TextureArrayRegistryImplementation textureArrayRegistry;
    private final TextureBinder textureBinder;

    public void bind(final ProgramEntityImplementation program, final Map<TextureType, TextureArrayEntityImplementation> textureArrays) {
        StreamEx.of(TextureType.values()).forEach(type -> bind(type, program, textureArrays.get(type)));
    }

    private void bind(final TextureType type, final ProgramEntityImplementation program, final TextureArrayEntityImplementation array) {
        textureBinder.bind(type, program, array);
        bind(program.getUniformID(type.getUseTextureUniform()), array != null && array != textureArrayRegistry.getDefaultTextureArray());
    }
}
