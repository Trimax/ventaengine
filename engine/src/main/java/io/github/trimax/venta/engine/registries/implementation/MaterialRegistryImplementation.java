package io.github.trimax.venta.engine.registries.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.registries.MaterialRegistry;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaterialRegistryImplementation
        extends AbstractRegistryImplementation<MaterialEntityImplementation, MaterialEntity, Void>
        implements MaterialRegistry {
    private final TextureRegistryImplementation textureRegistry;

    @Override
    protected MaterialEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        log.info("Loading material {}", resourcePath);

        final var materialDTO = ResourceUtil.loadAsObject(String.format("/materials/%s", resourcePath), MaterialDTO.class);

        final var material = new MaterialEntityImplementation(materialDTO);
        materialDTO.textures().forEach((textureType, path) -> material.setTexture(textureType, textureRegistry.get(path)));

        return material;
    }

    @Override
    protected void unload(@NonNull final MaterialEntityImplementation entity) {
        log.info("Unloading material {}", entity.getID());
    }
}