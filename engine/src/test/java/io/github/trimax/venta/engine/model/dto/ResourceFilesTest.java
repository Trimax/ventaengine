package io.github.trimax.venta.engine.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import io.github.trimax.venta.engine.model.dto.common.MeshDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

/** Every JSON resource of the engine and of the examples must match its DTO */
final class ResourceFilesTest {
    private static final List<Path> RESOURCE_ROOTS = List.of(Path.of("src/main/resources"), Path.of("../examples/src/main/resources"));

    private static final Map<String, Class<?>> DTO_BY_FOLDER = Map.ofEntries(
            Map.entry("billboards", BillboardDTO.class),
            Map.entry("cubemaps", CubemapDTO.class),
            Map.entry("emitters", EmitterDTO.class),
            Map.entry("gridmeshes", GridMeshDTO.class),
            Map.entry("lights", LightDTO.class),
            Map.entry("materials", MaterialDTO.class),
            Map.entry("meshes", MeshDTO.class),
            Map.entry("objects", ObjectDTO.class),
            Map.entry("programs", ProgramDTO.class),
            Map.entry("scenes", SceneDTO.class),
            Map.entry("soundsources", SoundSourceDTO.class),
            Map.entry("sprites", SpriteDTO.class),
            Map.entry("surfaces/terrain", TerrainSurfaceDTO.class),
            Map.entry("surfaces/water", WaterSurfaceDTO.class),
            Map.entry("texturearrays", TextureArrayDTO.class));

    @TestFactory
    Stream<DynamicTest> testResourcesMatchTheirDTO() throws IOException {
        final var tests = new ArrayList<DynamicTest>();
        for (final var root : RESOURCE_ROOTS)
            for (final var entry : DTO_BY_FOLDER.entrySet()) {
                final var folder = root.resolve(entry.getKey());
                if (!Files.isDirectory(folder))
                    continue;

                try (final var files = Files.walk(folder)) {
                    files.filter(file -> file.toString().endsWith(".json"))
                            .forEach(file -> tests.add(DynamicTest.dynamicTest(root.relativize(file).toString(),
                                    () -> parse(file, entry.getValue()))));
                }
            }

        assertFalse(tests.isEmpty(), "Resources must be found");
        return tests.stream();
    }

    private static void parse(final Path file, final Class<?> dtoClass) throws IOException {
        final var dto = assertDoesNotThrow(() -> ParsingUtil.asObject(Files.readString(file), dtoClass), file + " must match " + dtoClass.getSimpleName());
        assertNotNull(dto, file + " must not be empty");

        if (dto instanceof MeshDTO mesh)
            for (final var vertex : mesh.vertices())
                if (vertex.hasColor())
                    assertTrue(vertex.color().r() + vertex.color().g() + vertex.color().b() + vertex.color().a() > 0,
                            file + ": vertex color is all zeros, probably written with wrong keys");
    }
}
