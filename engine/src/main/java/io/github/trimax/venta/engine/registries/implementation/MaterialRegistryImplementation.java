package io.github.trimax.venta.engine.registries.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.parsers.MtlMaterialParser;
import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.implementation.Abettor;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.registries.MaterialRegistry;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaterialRegistryImplementation
        extends AbstractRegistryImplementation<MaterialEntityImplementation, MaterialEntity, Void>
        implements MaterialRegistry {
    /** Parsed material libraries (MTL files), their materials are registered as `library#material` */
    private final Map<String, Map<String, MaterialDTO>> libraries = new HashMap<>();

    private final TextureRegistryImplementation textureRegistry;
    private final MtlMaterialParser mtlMaterialParser;
    private final ResourceService resourceService;
    private final Abettor abettor;

    @Override
    protected MaterialEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        log.info("Loading material {}", resourcePath);

        return create(resourceService.getAsObject(String.format("/materials/%s", resourcePath), MaterialDTO.class));
    }

    /**
     * A material of a material library (an MTL file next to a model). The libraries are searched in their order.
     *
     * @param libraries the libraries, relative to the `meshes/` folder
     * @param name      the material name
     * @return the material or null when no library has it
     */
    public MaterialEntityImplementation get(@NonNull final List<String> libraries, @NonNull final String name) {
        for (final var library : libraries) {
            final var materials = this.libraries.computeIfAbsent(library, path -> mtlMaterialParser.parse(String.format("/meshes/%s", path)));
            if (materials.containsKey(name))
                return get(library + "#" + name, () -> create(materials.get(name)));
        }

        log.warn("Material {} isn't found in {}", name, libraries);
        return null;
    }

    private MaterialEntityImplementation create(@NonNull final MaterialDTO materialDTO) {
        final var material = abettor.createMaterial(materialDTO);
        if (materialDTO.hasTextures())
            materialDTO.textures().forEach((textureType, path) -> material.setTexture(textureType, textureRegistry.get(path, textureType.getColorSpace())));

        return material;
    }

    @Override
    protected void unload(@NonNull final MaterialEntityImplementation entity) {
        log.info("Unloading material {}", entity.getID());
        libraries.clear();
    }
}