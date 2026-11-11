package io.github.trimax.venta.engine.parsers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Vector2f;
import org.joml.Vector3f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.MeshFormat;
import io.github.trimax.venta.engine.model.dto.common.ColorDTO;
import io.github.trimax.venta.engine.model.dto.common.MeshDTO;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings("unused")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjMeshParser implements AbstractParser<MeshDTO> {
    private final ResourceService resourceService;

    @Override
    @SneakyThrows
    public MeshDTO parse(@NonNull final String resourcePath) {
        try (final var reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(resourceService.getAsBytes(resourcePath))))) {
            return ObjMeshParser.parse(reader);
        }
    }

    /** Parses Wavefront OBJ text: positions, texture coordinates, normals and polygonal faces */
    static MeshDTO parse(final BufferedReader reader) throws IOException {
        final List<Vector3f> positions = new ArrayList<>();
        final List<Vector3f> normals = new ArrayList<>();
        final List<Vector2f> textureCoordinates = new ArrayList<>();
        final List<MeshDTO.Vertex> vertices = new ArrayList<>();
        final List<MeshDTO.Facet> facets = new ArrayList<>();
        final Set<MeshDTO.Edge> edges = new LinkedHashSet<>();

        final Map<String, Integer> vertexCache = new HashMap<>();

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
                case "f" -> {
                    if (tokens.length < 4) {
                        log.warn("Face with less than 3 vertices (ignored): {}", line);
                        continue;
                    }

                    final var polygon = new int[tokens.length - 1];
                    for (int i = 1; i < tokens.length; i++)
                        polygon[i - 1] = addVertex(tokens[i], positions, textureCoordinates, normals, vertices, vertexCache);

                    addPolygon(polygon, facets, edges);
                }
                default -> log.debug("Unsupported OBJ statement (ignored): {}", line);
            }
        }

        return new MeshDTO(vertices, facets, List.copyOf(edges));
    }

    /** Triangulates a convex polygon as a fan; edges are the outline of the polygon */
    private static void addPolygon(final int[] polygon, final List<MeshDTO.Facet> facets, final Set<MeshDTO.Edge> edges) {
        for (int i = 1; i < polygon.length - 1; i++)
            facets.add(new MeshDTO.Facet(polygon[0], polygon[i], polygon[i + 1]));

        for (int i = 0; i < polygon.length; i++) {
            final var from = polygon[i];
            final var to = polygon[(i + 1) % polygon.length];
            edges.add(new MeshDTO.Edge(Math.min(from, to), Math.max(from, to)));
        }
    }

    /** Resolves a "v/vt/vn" reference to a vertex index, creating the vertex on first use */
    private static int addVertex(final String reference,
                                 final List<Vector3f> positions,
                                 final List<Vector2f> textureCoordinates,
                                 final List<Vector3f> normals,
                                 final List<MeshDTO.Vertex> vertices,
                                 final Map<String, Integer> vertexCache) {
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
