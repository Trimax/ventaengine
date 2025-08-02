package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.managers.MaterialManager;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.model.view.MaterialView;
import io.github.trimax.venta.engine.model.view.TextureView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector2f;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaterialManagerImplementation
        extends AbstractManagerImplementation<MaterialManagerImplementation.MaterialEntity, MaterialView>
        implements MaterialManager {
    private final ResourceManagerImplementation resourceManager;
    private final TextureManagerImplementation textureManager;

    @Override
    public MaterialEntity load(@NonNull final String name) {
        log.info("Loading material {}", name);

        final var materialDTO = resourceManager.load(String.format("/materials/%s.json", name), MaterialDTO.class);

        final var material = store(new MaterialEntity(name, materialDTO));
        materialDTO.textures().forEach((textureType, path) -> material.setTexture(textureType, textureManager.load(path)));

        return material;
    }

    @Override
    protected void destroy(final MaterialEntity material) {
        log.info("Destroying material {} ({})", material.getID(), material.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }

    @Getter
    public static final class MaterialEntity extends AbstractEntity implements MaterialView {
        private final Map<TextureType, TextureView> textures = new HashMap<>();
        private final Float shininess;
        private final Float opacity;
        private final Vector2f tiling;
        private final Vector2f offset;


        MaterialEntity(@NonNull final String name, final Float shininess, final Float opacity, final Vector2f tiling, final Vector2f offset) {
            super(name);

            this.shininess = shininess;
            this.opacity = opacity;
            this.tiling = tiling;
            this.offset = offset;
        }

        MaterialEntity(@NonNull final String name, @NonNull final MaterialDTO dto) {
            this(name, dto.shininess(), dto.opacity(), dto.tiling(), dto.offset());
        }

        @Override
        public void setTexture(final TextureType type, final TextureView texture) {
            this.textures.put(type, texture);
        }

        @Override
        public TextureView getTexture(final TextureType texture) {
            return this.textures.get(texture);
        }
    }
}
