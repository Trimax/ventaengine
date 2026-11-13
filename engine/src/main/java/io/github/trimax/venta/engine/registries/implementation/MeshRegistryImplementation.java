package io.github.trimax.venta.engine.registries.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.factories.MeshParserFactory;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.layouts.MeshVertexLayout;
import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshPart;
import io.github.trimax.venta.engine.model.dto.common.MeshDTO;
import io.github.trimax.venta.engine.model.entity.MeshEntity;
import io.github.trimax.venta.engine.model.entity.implementation.Abettor;
import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;
import io.github.trimax.venta.engine.parsers.ObjMeshParser;
import io.github.trimax.venta.engine.registries.MeshRegistry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.io.FilenameUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MeshRegistryImplementation
        extends AbstractRegistryImplementation<MeshEntityImplementation, MeshEntity, Void>
        implements MeshRegistry {
    /** Parts of the model files, their meshes are registered as `file#part` */
    private final Map<String, List<MeshPart>> parts = new HashMap<>();

    private final MeshParserFactory meshParserFactory;
    private final GeometryHelper geometryHelper;
    private final ObjMeshParser objMeshParser;
    private final Abettor abettor;

    @Override
    protected MeshEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        log.info("Loading mesh {}", resourcePath);

        return createMesh(resourcePath, meshParserFactory.get(resourcePath).parse(String.format("/meshes/%s", resourcePath)));
    }

    /**
     * Parts of an OBJ file: a mesh per group and material, with the material libraries relative to the `meshes/` folder.
     * Other formats have no parts.
     */
    public List<MeshPart> getParts(@NonNull final String resourcePath) {
        if (!"obj".equalsIgnoreCase(FilenameUtils.getExtension(resourcePath)))
            return List.of();

        return parts.computeIfAbsent(resourcePath, this::loadParts);
    }

    private List<MeshPart> loadParts(@NonNull final String resourcePath) {
        log.info("Loading mesh parts {}", resourcePath);

        final var model = objMeshParser.parseModel(String.format("/meshes/%s", resourcePath));
        final var folder = FilenameUtils.getPath(resourcePath);
        final var libraries = StreamEx.of(model.materialLibraries()).map(library -> folder + library).toList();

        return StreamEx.of(model.parts())
                .map(part -> {
                    final var name = resourcePath + "#" + part.name();
                    return new MeshPart(part.name(), get(name, () -> createMesh(name, part.mesh())), libraries, part.material());
                })
                .toList();
    }

    private MeshEntityImplementation createMesh(final String resourcePath, final MeshDTO mesh) {
        return abettor.createMesh(geometryHelper.create(resourcePath, MeshVertexLayout.class, mesh.getVerticesArray(),
                        mesh.hasFacets() ? mesh.getFacesArray() : null,
                        mesh.hasEdges() ? mesh.getEdgesArray() : null),
                BoundingBox.of(mesh));
    }

    @Override
    protected void unload(@NonNull final MeshEntityImplementation entity) {
        log.info("Unloading mesh {}", entity.getID());

        geometryHelper.delete(entity.getGeometry());
        parts.clear();
    }
}