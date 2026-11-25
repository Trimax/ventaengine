package io.github.trimax.venta.engine.repositories.implementation;

import io.github.trimax.venta.engine.utils.LimitUtil;
import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.TextureColorSpace;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.dto.TerrainSurfaceDTO;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.layouts.GridMeshVertexLayout;
import io.github.trimax.venta.engine.utils.HeightmapUtil;
import io.github.trimax.venta.engine.utils.TerrainChunkUtil;
import io.github.trimax.venta.engine.model.common.terrain.TerrainSlope;
import io.github.trimax.venta.engine.model.dto.terrain.TerrainMaterialDTO;
import io.github.trimax.venta.engine.model.dto.terrain.TerrainSlopeMaterialDTO;
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
    private final GeometryHelper geometryHelper;
    private final ResourceService resourceService;
    private final Abettor abettor;

    @Override
    protected TerrainSurfacePrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading terrain surface {}", resourcePath);

        final var terrainSurfaceDTO = resourceService.getAsObject(String.format("/surfaces/terrain/%s", resourcePath), TerrainSurfaceDTO.class);

        final var slopeDTOs = terrainSurfaceDTO.slopeMaterials() == null ? List.<TerrainSlopeMaterialDTO>of() : terrainSurfaceDTO.slopeMaterials();

        LimitUtil.checkCount(resourcePath, "materials", terrainSurfaceDTO.materials() == null ? 0 : terrainSurfaceDTO.materials().size(),
                1, DefinitionsCommon.MAX_MATERIALS - slopeDTOs.size());
        LimitUtil.checkCount(resourcePath, "slopeMaterials", slopeDTOs.size(), 0, TerrainSlope.MAXIMAL_COUNT);

        // The materials by the steepness are the last layers of the textures: after the ones of the heights
        final var materials = StreamEx.of(terrainSurfaceDTO.materials()).map(TerrainMaterialDTO::material)
                .append(StreamEx.of(slopeDTOs).map(TerrainSlopeMaterialDTO::material))
                .map(materialRegistry::get).toList();
        final var gridMesh = gridMeshRegistry.get(terrainSurfaceDTO.gridMesh());
        final var heightmap = textureRegistry.get(terrainSurfaceDTO.elevation().heightmap(), TextureColorSpace.Data);

        // The grid is cut into chunks (with their levels of detail and skirts), which have a geometry of their own: the water
        // draws the whole grid, and shares it with the terrain
        final var layout = TerrainChunkUtil.build(gridMesh.getSize(), gridMesh.getSegments(), HeightmapUtil.decode(heightmap.getID(), heightmap.getBuffer()));
        final var chunkGeometry = geometryHelper.create(resourcePath + " chunks", GridMeshVertexLayout.class, layout.getVertices(), layout.getIndices(), null);

        return abettor.createTerrainSurface(gridMesh,
                programRegistry.get(terrainSurfaceDTO.program()),
                heightmap,
                materials,
                createTextureArrays(resourcePath, materials),
                StreamEx.of(terrainSurfaceDTO.materials()).mapToDouble(TerrainMaterialDTO::elevation).toFloatArray(),
                StreamEx.of(slopeDTOs).map(TerrainSlope::new).toList(),
                terrainSurfaceDTO.elevation().factor(),
                layout,
                chunkGeometry,
                terrainSurfaceDTO.lodDistance() == null ? 0.f : terrainSurfaceDTO.lodDistance());
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

        geometryHelper.delete(entity.getChunkGeometry());
    }
}
