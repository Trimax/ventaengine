package io.github.trimax.venta.engine.managers;

import static io.github.trimax.venta.engine.definitions.Definitions.*;

import java.nio.ByteBuffer;

import io.github.trimax.venta.engine.enums.EntityType;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.exceptions.TextureBakeException;
import io.github.trimax.venta.engine.model.view.AtlasView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AtlasManager extends AbstractManager<AtlasManager.AtlasEntity, AtlasView> {
    private final TextureManager textureManager;
    private final TextureManager.TextureAccessor textureAccessor;

    private AtlasView create(final String name, final int i, final ByteBuffer fontBuffer) {
        final var bitmap = BufferUtils.createByteBuffer(FONT_ATLAS_WIDTH * FONT_ATLAS_HEIGHT);
        final var characterBuffer = STBTTBakedChar.malloc(FONT_ATLAS_CHARACTERS_COUNT);

        final var firstChar = i * FONT_ATLAS_CHARACTERS_COUNT;

        final var result = STBTruetype.stbtt_BakeFontBitmap(fontBuffer, FONT_HEIGHT, bitmap, FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT, firstChar, characterBuffer);
        if (result <= 0)
            throw new TextureBakeException("Failed to bake font bitmap atlas " + i);

        return store(new AtlasEntity(name, textureAccessor.get(textureManager.create(name, bitmap)), characterBuffer));
    }

    @Override
    protected void destroy(final AtlasEntity font) {
        log.info("Destroying atlas {} ({})", font.getID(), font.getName());
        font.buffer.free();
    }

    @Override
    protected boolean shouldCache() {
        return true;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.Atlas;
    }

    @Getter
    public static final class AtlasEntity extends AbstractEntity implements AtlasView {
        private final TextureManager.TextureEntity texture;
        private final STBTTBakedChar.Buffer buffer;

        AtlasEntity(@NonNull final String name,
                @NonNull final TextureManager.TextureEntity texture,
                @NonNull final STBTTBakedChar.Buffer buffer) {
            super(name);

            this.texture = texture;
            this.buffer = buffer;

            printUsageSummary();
        }

        private void printUsageSummary() {
            int maxX = 0;
            int maxY = 0;
            int usedChars = 0;

            for (int i = 0; i < FONT_ATLAS_CHARACTERS_COUNT; i++) {
                final var backedCharacter = buffer.get(i);
                final var width = backedCharacter.x1() - backedCharacter.x0();
                final var height = backedCharacter.y1() - backedCharacter.y0();
                if (width > 0 && height > 0) {
                    usedChars++;
                    if (backedCharacter.x1() > maxX)
                        maxX = backedCharacter.x1();

                    if (backedCharacter.y1() > maxY)
                        maxY = backedCharacter.y1();
                }
            }

            log.debug("Atlas {}: used chars = {}, maxX = {}, maxY = {} (atlas size: {} x {}). Used: {}%",
                    getName(), usedChars, maxX, maxY, FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT,
                    String.format("%2.2f", 100.f * (float) (maxX * maxY) / (float) (FONT_ATLAS_WIDTH * FONT_ATLAS_HEIGHT)));
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class AtlasAccessor extends AbstractAccessor {
        public AtlasEntity create(final String name, final int i, final ByteBuffer fontBuffer) {
            return get(AtlasManager.this.create(name, i, fontBuffer));
        }
    }
}
