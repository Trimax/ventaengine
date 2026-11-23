package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.github.trimax.venta.engine.model.common.shader.ShaderSource;
import io.github.trimax.venta.engine.model.dto.ProgramDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;
import io.github.trimax.venta.engine.utils.PreprocessingUtil;

/**
 * The shaders and the programs of the engine as the resources of the classpath.
 */
public final class ShaderResources {
    private ShaderResources() {
    }

    /** The text of a shader file by its path relative to the shaders folder, null when there is no such file */
    public static String load(final String path) {
        try (final var stream = ShaderResources.class.getResourceAsStream("/shaders/" + path)) {
            return stream == null ? null : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /** The shader with its includes expanded */
    public static ShaderSource expand(final String shader) {
        return PreprocessingUtil.process(shader, ShaderResources::load);
    }

    public static List<String> shaders(final ProgramDTO program) {
        return List.of(program.shaderVertex(), program.shaderFragment());
    }

    public static List<String> allShaders() throws Exception {
        return programs().values().stream().flatMap(program -> shaders(program).stream()).toList();
    }

    /** The programs by the names of their definition files */
    public static Map<String, ProgramDTO> programs() throws Exception {
        final Map<String, ProgramDTO> programs = new HashMap<>();
        try (final var files = Files.list(resources("/programs"))) {
            for (final var file : files.filter(path -> path.toString().endsWith(".json")).toList())
                programs.put(file.getFileName().toString(), ParsingUtil.asObject(Files.readString(file), ProgramDTO.class));
        }

        assertFalse(programs.isEmpty(), "The programs must be found");
        return programs;
    }

    /** All the shader files, including the common ones, relative to the shaders folder */
    public static List<String> shaderFiles() throws Exception {
        final var folder = resources("/shaders");
        try (final Stream<Path> files = Files.walk(folder)) {
            return files.filter(path -> path.toString().endsWith(".glsl")).map(path -> folder.relativize(path).toString().replace('\\', '/')).toList();
        }
    }

    private static Path resources(final String folder) throws URISyntaxException {
        return Path.of(ShaderResources.class.getResource(folder).toURI());
    }
}
