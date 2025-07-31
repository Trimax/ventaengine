package io.github.trimax.venta.engine.managers;

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
        private final int internalID;
        private final int width;
        private final int height;

        FontEntity(final int internalID,
                @NonNull final String name,
                final int width,
                final int height) {
            super(name);

            this.internalID = internalID;
            this.width = width;
            this.height = height;
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class FontAccessor extends AbstractManager.AbstractAccessor {}
}
