package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.FontManager;
import io.github.trimax.venta.engine.model.entity.FontEntity;
import io.github.trimax.venta.engine.model.view.FontView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.system.MemoryUtil;

import static io.github.trimax.venta.engine.definitions.Definitions.FONT_ATLAS_COUNT;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FontManagerImplementation
        extends AbstractManagerImplementation<FontEntity, FontView>
        implements FontManager {
    private final AtlasManagerImplementation atlasManagerImplementation;
    private final ResourceManagerImplementation resourceManager;

    public FontEntity load(@NonNull final String name) {
        final var buffer = resourceManager.loadAsBuffer(String.format("/fonts/%s.ttf", name));

        final var font = new FontEntity(name, buffer);
        for (int i = 0; i < FONT_ATLAS_COUNT; i++)
            font.add(atlasManagerImplementation.create(String.format("%s-%d", name, i), i, buffer));

        return store(font);
    }

    @Override
    protected void destroy(final FontEntity font) {
        log.info("Destroying font {} ({})", font.getID(), font.getName());
        MemoryUtil.memFree(font.getBuffer());
    }

    @Override
    protected boolean shouldCache() {
        return true;
    }
}
