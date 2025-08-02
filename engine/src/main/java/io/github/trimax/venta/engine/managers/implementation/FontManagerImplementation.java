package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.EntityType;
import io.github.trimax.venta.engine.model.view.FontView;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static io.github.trimax.venta.engine.definitions.Definitions.FONT_ATLAS_COUNT;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FontManagerImplementation extends AbstractManagerImplementation<FontManagerImplementation.FontEntity, FontView> {
    private final AtlasManagerImplementation.AtlasAccessor atlasAccessor;
    private final ResourceManagerImplementation resourceManager;

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

    @Override
    public EntityType getEntityType() {
        return EntityType.Font;
    }

    @Getter
    public static final class FontEntity extends AbstractManagerImplementation.AbstractEntity implements FontView {
        private final List<AtlasManagerImplementation.AtlasEntity> atlases = new ArrayList<>();

        FontEntity(@NonNull final String name) {
            super(name);
        }

        public void add(final AtlasManagerImplementation.AtlasEntity atlas) {
            this.atlases.add(atlas);
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class FontAccessor extends AbstractAccessor {}
}
