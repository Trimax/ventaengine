package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.exceptions.TextureBakeException;
import io.github.trimax.venta.engine.managers.AtlasManager;
import io.github.trimax.venta.engine.model.entity.AtlasEntity;
import io.github.trimax.venta.engine.model.view.AtlasView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static io.github.trimax.venta.engine.definitions.Definitions.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AtlasManagerImplementation
        extends AbstractManagerImplementation<AtlasEntity, AtlasView>
        implements AtlasManager {
    private final TextureManagerImplementation textureManager;

    public AtlasEntity create(final String name, final int i, final ByteBuffer fontBuffer) {
        final var bitmap = BufferUtils.createByteBuffer(FONT_ATLAS_WIDTH * FONT_ATLAS_HEIGHT);
        final var characterBuffer = STBTTBakedChar.malloc(FONT_ATLAS_CHARACTERS_COUNT);

        final var firstChar = i * FONT_ATLAS_CHARACTERS_COUNT;

        final var result = STBTruetype.stbtt_BakeFontBitmap(fontBuffer, FONT_HEIGHT, bitmap, FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT, firstChar, characterBuffer);
        if (result <= 0)
            throw new TextureBakeException("Failed to bake font bitmap atlas " + i);

        final var atlas = new AtlasEntity(name, textureManager.create(name, bitmap), characterBuffer);
        MemoryUtil.memFree(bitmap);

        return store(atlas);
    }

    @Override
    protected void destroy(final AtlasEntity font) {
        log.info("Destroying atlas {} ({})", font.getID(), font.getName());
        font.getBuffer().free();
    }

    @Override
    protected boolean shouldCache() {
        return true;
    }
}
