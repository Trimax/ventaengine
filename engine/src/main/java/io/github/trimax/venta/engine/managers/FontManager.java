package io.github.trimax.venta.engine.managers;

import static io.github.trimax.venta.engine.definitions.Definitions.FONT_ATLAS_CHARACTERS_COUNT;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.view.FontView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FontManager extends AbstractManager<FontManager.FontEntity, FontView> {
    private final ResourceManager resourceManager;
    private final AtlasManager atlasManager;

    private final AtlasManager.AtlasAccessor atlasAccessor;

    public FontView create(final String name) {
        final var bytes = resourceManager.loadAsBytes(String.format("/fonts/%s.ttf", name));

        final var fontBuffer = BufferUtils.createByteBuffer(bytes.length);
        fontBuffer.put(bytes);
        fontBuffer.flip();

        // Support BMP Unicode (0..0xFFFF)
        final var atlasesCount = (65536 + FONT_ATLAS_CHARACTERS_COUNT - 1) / FONT_ATLAS_CHARACTERS_COUNT;

        final var atlases = new ArrayList<AtlasManager.AtlasEntity>();
        for (int i = 0; i < atlasesCount; i++)
            atlases.add(atlasAccessor.get(atlasManager.create(String.format("%s-%d", name, i), i, fontBuffer)));

        return store(new FontEntity(name, atlases));
    }

    @Override
    protected void destroy(final FontEntity font) {
        log.info("Destroying font {} ({})", font.getID(), font.getName());
    }

    @Override
    protected boolean shouldCache() {
        return true;
    }

    @Getter
    public static final class FontEntity extends AbstractManager.AbstractEntity implements FontView {
        private final List<AtlasManager.AtlasEntity> atlases;

        FontEntity(@NonNull final String name, @NonNull final List<AtlasManager.AtlasEntity> atlases) {
            super(name);

            this.atlases = atlases;
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class FontAccessor extends AbstractAccessor {}
}
