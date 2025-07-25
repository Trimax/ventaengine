package com.venta.engine.managers;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector2f;

import com.venta.engine.annotations.Component;
import com.venta.engine.enums.TextureType;
import com.venta.engine.model.dto.MaterialDTO;
import com.venta.engine.model.view.MaterialView;
import com.venta.engine.model.view.TextureView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaterialManager extends AbstractManager<MaterialManager.MaterialEntity, MaterialView> {
    private final ResourceManager resourceManager;
    private final TextureManager textureManager;

    public MaterialView load(final String name) {
        log.info("Loading material {}", name);

        final var materialDTO = resourceManager.load(String.format("/materials/%s", name), MaterialDTO.class);

        final var material = store(new MaterialEntity(materialDTO));
        materialDTO.textures().forEach((textureType, path) -> material.setTexture(textureType, textureManager.load(path)));

        return material;
    }

    @Override
    protected void destroy(final MaterialEntity material) {
        log.info("Deleting material {}", material.getName());
    }

    @Getter
    public static final class MaterialEntity extends AbstractEntity implements
            com.venta.engine.model.view.MaterialView {
        private final Map<TextureType, TextureView> textures = new HashMap<>();
        private final String name;
        private final Float shininess;
        private final Float opacity;
        private final Vector2f tiling;
        private final Vector2f offset;


        MaterialEntity(@NonNull final String name, final Float shininess, final Float opacity, final Vector2f tiling, final Vector2f offset) {
            this.name = name;
            this.shininess = shininess;
            this.opacity = opacity;
            this.tiling = tiling;
            this.offset = offset;
        }

        MaterialEntity(@NonNull final MaterialDTO dto) {
            this(dto.name(), dto.shininess(), dto.opacity(), dto.tiling(), dto.offset());
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

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class MaterialAccessor extends AbstractAccessor {}
}
