package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.exceptions.TextureBakeException;
import io.github.trimax.venta.engine.managers.AtlasManager;
import io.github.trimax.venta.engine.model.instance.AtlasInstance;
import io.github.trimax.venta.engine.model.instance.implementation.AtlasInstanceImplementation;
import io.github.trimax.venta.engine.registries.implementation.TextureRegistryImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;

import java.nio.ByteBuffer;

import static io.github.trimax.venta.engine.definitions.Definitions.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AtlasManagerImplementation
        extends AbstractManagerImplementation<AtlasInstanceImplementation, AtlasInstance>
        implements AtlasManager {
    private final TextureRegistryImplementation textureManager;

    public AtlasInstanceImplementation create(final String name, final int i, final ByteBuffer fontBuffer) {
        final var bitmap = BufferUtils.createByteBuffer(FONT_ATLAS_WIDTH * FONT_ATLAS_HEIGHT);
        final var characterBuffer = STBTTBakedChar.malloc(FONT_ATLAS_CHARACTERS_COUNT);

        final var firstChar = i * FONT_ATLAS_CHARACTERS_COUNT;

        final var result = STBTruetype.stbtt_BakeFontBitmap(fontBuffer, FONT_HEIGHT, bitmap, FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT, firstChar, characterBuffer);
        if (result <= 0)
            throw new TextureBakeException("Failed to bake font bitmap atlas " + i);

        return store(new AtlasInstanceImplementation(name, textureManager.create(name, bitmap), characterBuffer));
    }

    @Override
    protected void destroy(final AtlasInstanceImplementation font) {
        log.info("Destroying atlas {} ({})", font.getID(), font.getName());
        font.getBuffer().free();
    }

    @Override
    protected boolean shouldCache() {
        return true;
    }
}
