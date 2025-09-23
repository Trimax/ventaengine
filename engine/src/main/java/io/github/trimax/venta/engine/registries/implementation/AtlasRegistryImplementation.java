package io.github.trimax.venta.engine.registries.implementation;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.NotImplementedException;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.DefinitionsFont;
import io.github.trimax.venta.engine.exceptions.TextureBakeException;
import io.github.trimax.venta.engine.model.entity.AtlasEntity;
import io.github.trimax.venta.engine.model.entity.implementation.Abettor;
import io.github.trimax.venta.engine.model.entity.implementation.AtlasEntityImplementation;
import io.github.trimax.venta.engine.registries.AtlasRegistry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AtlasRegistryImplementation
        extends AbstractRegistryImplementation<AtlasEntityImplementation, AtlasEntity, Void>
        implements AtlasRegistry {
    private final TextureRegistryImplementation textureRepository;
    private final Abettor abettor;

    public AtlasEntityImplementation create(final String name, final int i, final ByteBuffer fontBuffer) {
        return get(name, () -> {
            final var bitmap = BufferUtils.createByteBuffer(DefinitionsFont.ATLAS_WIDTH * DefinitionsFont.ATLAS_HEIGHT);
            final var characterBuffer = STBTTBakedChar.malloc(DefinitionsFont.ATLAS_COUNT_CHARACTERS);

            final var firstChar = i * DefinitionsFont.ATLAS_COUNT_CHARACTERS;

            final var result = STBTruetype.stbtt_BakeFontBitmap(fontBuffer, DefinitionsFont.FONT_HEIGHT, bitmap, DefinitionsFont.ATLAS_WIDTH, DefinitionsFont.ATLAS_HEIGHT, firstChar, characterBuffer);
            if (result <= 0)
                throw new TextureBakeException("Failed to bake font bitmap atlas " + i);

            return abettor.createAtlas(textureRepository.create(name, bitmap), characterBuffer);
        });
    }

    @Override
    protected AtlasEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        throw new NotImplementedException("Loading atlas from the resource is not implemented");
    }

    @Override
    protected void unload(@NonNull final AtlasEntityImplementation entity) {
        log.info("Unloading atlas {}", entity.getID());
        entity.getBuffer().free();
    }
}
