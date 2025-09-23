package io.github.trimax.venta.engine.parsers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector2f;
import org.joml.Vector3f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.MeshFormat;
import io.github.trimax.venta.engine.model.common.dto.Color;
import io.github.trimax.venta.engine.model.dto.common.Mesh;
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
public final class ObjMeshParser implements AbstractParser<Mesh> {
    private final ResourceService resourceService;

    @Override
    @SneakyThrows
    public Mesh parse(@NonNull final String resourcePath) {
        try (final var reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(resourceService.getAsBytes(resourcePath))))) {
            return parse(reader);
        }
    }

    private Mesh parse(final BufferedReader reader) throws IOException {
        final List<Vector3f> positions = new ArrayList<>();
        final List<Vector3f> normals = new ArrayList<>();
        final List<Vector2f> textureCoordinates = new ArrayList<>();
        final List<Mesh.Vertex> vertices = new ArrayList<>();
        final List<Mesh.Facet> facets = new ArrayList<>();
        final List<Mesh.Edge> edges = new ArrayList<>();

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
                    if (tokens.length == 4) {
                        // The face is a triangle
                        addFace(tokens[1], tokens[2], tokens[3],
                                positions, textureCoordinates, normals,
                                vertices, facets, edges, vertexCache);
                    } else if (tokens.length == 5) {
                        // The face is a square
                        addFace(tokens[1], tokens[2], tokens[3],
                                positions, textureCoordinates, normals,
                                vertices, facets, edges, vertexCache);
                        addFace(tokens[1], tokens[3], tokens[4],
                                positions, textureCoordinates, normals,
                                vertices, facets, edges, vertexCache);
                    } else
                        log.warn("Unsupported facet (ignored): {}", line);
                }
                default -> log.warn("Unsupported type: {}", line);
            }
        }

        return new Mesh(vertices, facets, edges);
    }

    private static void addFace(final String a, final String b, final String c,
                                final List<Vector3f> positions,
                                final List<Vector2f> textureCoordinates,
                                final List<Vector3f> normals,
                                final List<Mesh.Vertex> vertices,
                                final List<Mesh.Facet> facets,
                                final List<Mesh.Edge> edges,
                                final Map<String, Integer> vertexCache) {
        final String[] arr = {a, b, c};
        final var faceIndices = new int[3];

        for (int i = 0; i < 3; i++) {
            final var parts = arr[i].split("/");
            final var vIdx = Integer.parseInt(parts[0]) - 1;

            final var pos = positions.get(vIdx);

            final var uv = (parts.length > 1 && !parts[1].isEmpty())
                    ? textureCoordinates.get(Integer.parseInt(parts[1]) - 1)
                    : null;

            final var normal = (parts.length > 2 && !parts[2].isEmpty())
                    ? normals.get(Integer.parseInt(parts[2]) - 1)
                    : null;

            final var key = Arrays.toString(parts);
            final var vertexIndex = vertexCache.computeIfAbsent(key, _ -> {
                vertices.add(new Mesh.Vertex(pos, normal, uv, new Color(1, 1, 1, 1)));
                return vertices.size() - 1;
            });

            faceIndices[i] = vertexIndex;
        }

        facets.add(new Mesh.Facet(faceIndices[0], faceIndices[1], faceIndices[2]));

        edges.add(new Mesh.Edge(faceIndices[0], faceIndices[1]));
        edges.add(new Mesh.Edge(faceIndices[1], faceIndices[2]));
        edges.add(new Mesh.Edge(faceIndices[2], faceIndices[0]));
    }

    @Override
    public MeshFormat format() {
        return MeshFormat.OBJ;
    }
}
