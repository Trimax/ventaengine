package io.github.trimax.venta.engine.managers;

import static io.github.trimax.venta.engine.definitions.Definitions.FONT_ATLAS_COUNT;

import java.util.ArrayList;
import java.util.List;

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
    private final AtlasManager.AtlasAccessor atlasAccessor;
    private final ResourceManager resourceManager;

    public FontView create(final String name) {
        final var fontBuffer = resourceManager.loadAsBuffer(String.format("/fonts/%s.ttf", name));

        final var font = new FontEntity(name);
        for (int i = 0; i < FONT_ATLAS_COUNT; i++)
            font.add(atlasAccessor.create(String.format("%s-%d", name, i), i, fontBuffer));

        return store(font);
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
        private final List<AtlasManager.AtlasEntity> atlases = new ArrayList<>();

        FontEntity(@NonNull final String name) {
            super(name);
        }

        public void add(final AtlasManager.AtlasEntity atlas) {
            this.atlases.add(atlas);
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class FontAccessor extends AbstractAccessor {}
}
