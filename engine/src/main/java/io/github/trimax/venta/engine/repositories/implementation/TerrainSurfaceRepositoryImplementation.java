package io.github.trimax.venta.engine.repositories.implementation;

import io.github.trimax.venta.engine.utils.LimitUtil;
import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.TextureColorSpace;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.dto.TerrainSurfaceDTO;
import io.github.trimax.venta.engine.model.dto.terrain.TerrainMaterialDTO;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureArrayEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import io.github.trimax.venta.engine.model.prefabs.TerrainSurfacePrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.Abettor;
import io.github.trimax.venta.engine.model.prefabs.implementation.TerrainSurfacePrefabImplementation;
import io.github.trimax.venta.engine.registries.implementation.*;
import io.github.trimax.venta.engine.repositories.TerrainSurfaceRepository;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TerrainSurfaceRepositoryImplementation
        extends AbstractRepositoryImplementation<TerrainSurfacePrefabImplementation, TerrainSurfacePrefab>
        implements TerrainSurfaceRepository {
    private final TextureArrayRegistryImplementation textureArrayRegistry;
    private final MaterialRegistryImplementation materialRegistry;
    private final GridMeshRegistryImplementation gridMeshRegistry;
    private final ProgramRegistryImplementation programRegistry;
    private final TextureRegistryImplementation textureRegistry;
    private final ResourceService resourceService;
    private final Abettor abettor;

    @Override
    protected TerrainSurfacePrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading terrain surface {}", resourcePath);

        final var terrainSurfaceDTO = resourceService.getAsObject(String.format("/surfaces/terrain/%s", resourcePath), TerrainSurfaceDTO.class);

        LimitUtil.checkCount(resourcePath, "materials", terrainSurfaceDTO.materials() == null ? 0 : terrainSurfaceDTO.materials().size(),
                1, DefinitionsCommon.MAX_MATERIALS);
        final var materials = StreamEx.of(terrainSurfaceDTO.materials()).map(TerrainMaterialDTO::material).map(materialRegistry::get).toList();
        return abettor.createTerrainSurface(gridMeshRegistry.get(terrainSurfaceDTO.gridMesh()),
                programRegistry.get(terrainSurfaceDTO.program()),
                textureRegistry.get(terrainSurfaceDTO.elevation().heightmap(), TextureColorSpace.Data),
                materials,
                createTextureArrays(resourcePath, materials),
                StreamEx.of(terrainSurfaceDTO.materials()).mapToDouble(TerrainMaterialDTO::elevation).toFloatArray(),
                terrainSurfaceDTO.elevation().factor());
    }

    private Map<TextureType, TextureArrayEntityImplementation> createTextureArrays(@NonNull final String name, @NonNull final List<MaterialEntityImplementation> materials) {
        final var groupedTextures = StreamEx.of(TextureType.values()).toMap(Function.identity(), type -> getTexturesByType(materials, type));

        return StreamEx.ofKeys(groupedTextures).toMap(Function.identity(),
                type -> createTextureArray(name, type, groupedTextures.get(type), materials.size()));
    }

    private TextureArrayEntityImplementation createTextureArray(@NonNull final String name, @NonNull final TextureType type,
                                                                @NonNull final List<TextureEntityImplementation> textures, final int materialsCount) {
        if (textures.isEmpty())
            return textureArrayRegistry.getDefaultTextureArray();

        // Layer index must match material index, so a texture type is used only if all the materials have it
        if (textures.size() != materialsCount) {
            log.warn("Terrain surface {}: only {} of {} materials have {} texture. Default texture array is used",
                    name, textures.size(), materialsCount, type);
            return textureArrayRegistry.getDefaultTextureArray();
        }

        return textureArrayRegistry.create(String.format("%s/%s", name, type.name()), textures);
    }

    private static List<TextureEntityImplementation> getTexturesByType(@NonNull final List<MaterialEntityImplementation> materials, @NonNull final TextureType type) {
        return StreamEx.of(materials)
                .map(material -> material.getTextures().get(type))
                .nonNull()
                .toList();
    }

    @Override
    protected void unload(@NonNull final TerrainSurfacePrefabImplementation entity) {
        log.info("Unloading terrain surface {}", entity.getID());
    }
}
