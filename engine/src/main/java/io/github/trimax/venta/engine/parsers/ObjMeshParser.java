package io.github.trimax.venta.engine.parsers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.joml.Vector2f;
import org.joml.Vector3f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.MeshFormat;
import io.github.trimax.venta.engine.model.dto.common.ColorDTO;
import io.github.trimax.venta.engine.model.dto.common.MeshDTO;
import io.github.trimax.venta.engine.model.dto.common.ObjModelDTO;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@SuppressWarnings("unused")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjMeshParser implements AbstractParser<MeshDTO> {
    /** Faces before the first `g` or `o` belong to this group */
    private static final String DEFAULT_GROUP = "default";

    private final ResourceService resourceService;

    @Override
    @SneakyThrows
    public MeshDTO parse(@NonNull final String resourcePath) {
        try (final var reader = open(resourcePath)) {
            return ObjMeshParser.parse(reader);
        }
    }

    /** Parses Wavefront OBJ text into one mesh: positions, texture coordinates, normals and polygonal faces */
    static MeshDTO parse(final BufferedReader reader) throws IOException {
        final var model = read(reader, false);
        return model.parts().isEmpty()
                ? new MeshDTO(List.of(), List.of(), List.of())
                : model.parts().getFirst().mesh();
    }

    /** Parses the model split into parts: a mesh per group and material */
    @SneakyThrows
    public ObjModelDTO parseModel(@NonNull final String resourcePath) {
        try (final var reader = open(resourcePath)) {
            return ObjMeshParser.parseModel(reader);
        }
    }

    /** Parses Wavefront OBJ text into parts: every group or object with every material used in it is a mesh */
    static ObjModelDTO parseModel(final BufferedReader reader) throws IOException {
        return read(reader, true);
    }

    private BufferedReader open(@NonNull final String resourcePath) {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(resourceService.getAsBytes(resourcePath))));
    }

    private static ObjModelDTO read(final BufferedReader reader, final boolean split) throws IOException {
        final List<Vector3f> positions = new ArrayList<>();
        final List<Vector3f> normals = new ArrayList<>();
        final List<Vector2f> textureCoordinates = new ArrayList<>();
        final List<String> materialLibraries = new ArrayList<>();
        final Map<String, PartBuilder> parts = new LinkedHashMap<>();

        var group = DEFAULT_GROUP;
        String material = null;
        PartBuilder part = null;

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
                continue;

            final var tokens = line.split("\\s+");
            switch (tokens[0]) {
                case "v" -> positions.add(new Vector3f(
                        Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2]),
                        Float.parseFloat(tokens[3])
                ));
                case "vt" -> textureCoordinates.add(new Vector2f(
                        Float.parseFloat(tokens[1]),
                        1.f - Float.parseFloat(tokens[2])
                ));
                case "vn" -> normals.add(new Vector3f(
                        Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2]),
                        Float.parseFloat(tokens[3])
                ));
                case "g", "o" -> {
                    group = tokens.length > 1 ? getArgument(tokens) : DEFAULT_GROUP;
                    part = null;
                }
                case "usemtl" -> {
                    material = tokens.length > 1 ? getArgument(tokens) : null;
                    part = null;
                }
                case "mtllib" -> materialLibraries.addAll(Arrays.asList(tokens).subList(1, tokens.length));
                case "f" -> {
                    if (tokens.length < 4) {
                        log.warn("Face with less than 3 vertices (ignored): {}", line);
                        continue;
                    }

                    if (part == null)
                        part = getPart(parts, split ? group : DEFAULT_GROUP, split ? material : null);

                    final var polygon = new int[tokens.length - 1];
                    for (int i = 1; i < tokens.length; i++)
                        polygon[i - 1] = part.addVertex(tokens[i], positions, textureCoordinates, normals);

                    part.addPolygon(polygon);
                }
                default -> log.debug("Unsupported OBJ statement (ignored): {}", line);
            }
        }

        return new ObjModelDTO(buildParts(parts.values()), List.copyOf(materialLibraries));
    }

    private static String getArgument(final String[] tokens) {
        return String.join(" ", Arrays.asList(tokens).subList(1, tokens.length));
    }

    private static PartBuilder getPart(final Map<String, PartBuilder> parts, final String group, final String material) {
        return parts.computeIfAbsent(group + '\u0000' + material, _ -> new PartBuilder(group, material));
    }

    /** A part is named after its group; a group with several materials has a part per material named after both */
    private static List<ObjModelDTO.Part> buildParts(final Collection<PartBuilder> parts) {
        final var materialsPerGroup = StreamEx.of(parts).groupingBy(PartBuilder::getGroup, Collectors.counting());

        return StreamEx.of(parts)
                .map(part -> new ObjModelDTO.Part(
                        materialsPerGroup.get(part.getGroup()) > 1 ? part.getGroup() + " (" + part.getMaterial() + ")" : part.getGroup(),
                        part.getMaterial(),
                        part.build()))
                .toList();
    }

    @Getter
    @RequiredArgsConstructor
    private static final class PartBuilder {
        private final String group;
        private final String material;

        private final List<MeshDTO.Vertex> vertices = new ArrayList<>();
        private final List<MeshDTO.Facet> facets = new ArrayList<>();
        private final Set<MeshDTO.Edge> edges = new LinkedHashSet<>();
        private final Map<String, Integer> vertexCache = new HashMap<>();

        private MeshDTO build() {
            return new MeshDTO(vertices, facets, List.copyOf(edges));
        }

        /** Triangulates a convex polygon as a fan; edges are the outline of the polygon */
        private void addPolygon(final int[] polygon) {
            for (int i = 1; i < polygon.length - 1; i++)
                facets.add(new MeshDTO.Facet(polygon[0], polygon[i], polygon[i + 1]));

            for (int i = 0; i < polygon.length; i++) {
                final var from = polygon[i];
                final var to = polygon[(i + 1) % polygon.length];
                edges.add(new MeshDTO.Edge(Math.min(from, to), Math.max(from, to)));
            }
        }

        /** Resolves a "v/vt/vn" reference to a vertex index of the part, creating the vertex on first use */
        private int addVertex(final String reference,
                              final List<Vector3f> positions,
                              final List<Vector2f> textureCoordinates,
                              final List<Vector3f> normals) {
            final var parts = reference.split("/");
            final var positionIndex = resolveIndex(parts[0], positions.size());
            final var textureIndex = parts.length > 1 && !parts[1].isEmpty() ? resolveIndex(parts[1], textureCoordinates.size()) : -1;
            final var normalIndex = parts.length > 2 && !parts[2].isEmpty() ? resolveIndex(parts[2], normals.size()) : -1;

            // Absolute indices, so relative and absolute references to the same data share a vertex
            final var key = positionIndex + "/" + textureIndex + "/" + normalIndex;
            return vertexCache.computeIfAbsent(key, _ -> {
                vertices.add(new MeshDTO.Vertex(positions.get(positionIndex),
                        normalIndex >= 0 ? normals.get(normalIndex) : null,
                        textureIndex >= 0 ? textureCoordinates.get(textureIndex) : null,
                        new ColorDTO(1, 1, 1, 1)));
                return vertices.size() - 1;
            });
        }
    }

    /** OBJ indices start at 1; negative indices count back from the last element defined so far */
    private static int resolveIndex(final String value, final int count) {
        final var index = Integer.parseInt(value);
        final var resolved = index > 0 ? index - 1 : count + index;
        if (index == 0 || resolved < 0 || resolved >= count)
            throw new IllegalArgumentException(String.format("OBJ index %d is out of range [1, %d]", index, count));

        return resolved;
    }

    @Override
    public MeshFormat format() {
        return MeshFormat.OBJ;
    }
}
