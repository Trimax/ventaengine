package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.MaterialManager;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.model.instance.MaterialInstance;
import io.github.trimax.venta.engine.model.instance.implementation.MaterialInstanceImplementation;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaterialManagerImplementation
        extends AbstractManagerImplementation<MaterialInstanceImplementation, MaterialInstance>
        implements MaterialManager {
    private final TextureManagerImplementation textureManager;

    @Override
    public MaterialInstanceImplementation load(@NonNull final String name) {
        log.info("Loading material {}", name);

        final var materialDTO = ResourceUtil.loadAsObject(String.format("/materials/%s.json", name), MaterialDTO.class);

        final var material = store(new MaterialInstanceImplementation(name, materialDTO));
        materialDTO.textures().forEach((textureType, path) -> material.setTexture(textureType, textureManager.load(path)));

        return material;
    }

    @Override
    protected void destroy(final MaterialInstanceImplementation material) {
        log.info("Destroying material {} ({})", material.getID(), material.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }
}
