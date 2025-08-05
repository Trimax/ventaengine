package io.github.trimax.venta.engine.registries.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.exceptions.TextureBakeException;
import io.github.trimax.venta.engine.model.entity.AtlasEntity;
import io.github.trimax.venta.engine.model.entity.implementation.AtlasEntityImplementation;
import io.github.trimax.venta.engine.registries.AtlasRegistry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;

import java.nio.ByteBuffer;

import static io.github.trimax.venta.engine.definitions.Definitions.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AtlasRegistryImplementation
        extends AbstractRegistryImplementation<AtlasEntityImplementation, AtlasEntity, Void>
        implements AtlasRegistry {
    private final TextureRegistryImplementation textureRepository;

    public AtlasEntityImplementation create(final String name, final int i, final ByteBuffer fontBuffer) {
        return get(name, () -> {
            final var bitmap = BufferUtils.createByteBuffer(FONT_ATLAS_WIDTH * FONT_ATLAS_HEIGHT);
            final var characterBuffer = STBTTBakedChar.malloc(FONT_ATLAS_CHARACTERS_COUNT);

            final var firstChar = i * FONT_ATLAS_CHARACTERS_COUNT;

            final var result = STBTruetype.stbtt_BakeFontBitmap(fontBuffer, FONT_HEIGHT, bitmap, FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT, firstChar, characterBuffer);
            if (result <= 0)
                throw new TextureBakeException("Failed to bake font bitmap atlas " + i);

            return new AtlasEntityImplementation(textureRepository.create(name, bitmap), characterBuffer);
        });
    }

    @Override
    protected AtlasEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        throw new NotImplementedException("Loading atlas from the resource is not implemented");
    }

    @Override
    protected void unload(@NonNull final AtlasEntityImplementation entity) {
        log.info("Unloading atlas prefab {}", entity.getID());
        entity.getBuffer().free();
    }
}
