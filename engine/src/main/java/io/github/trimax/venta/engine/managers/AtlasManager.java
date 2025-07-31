package io.github.trimax.venta.engine.managers;

import org.lwjgl.stb.STBTTBakedChar;

import io.github.trimax.venta.container.annotations.Component;
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

    @Override
    protected void destroy(final AtlasEntity font) {
        log.info("Destroying atlas {} ({})", font.getID(), font.getName());
    }

    @Override
    protected boolean shouldCache() {
        return true;
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
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class AtlasAccessor extends AbstractAccessor {}
}
