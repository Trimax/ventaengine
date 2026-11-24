package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.opengl.GL20C.*;

import java.util.HashMap;
import java.util.Map;

/**
 * The programs of the engine for the tests with OpenGL: compiled from the resources by the name of their definition file,
 * once for all the tests.
 */
public final class TestPrograms {
    private static final Map<String, Integer> PROGRAMS = new HashMap<>();

    private TestPrograms() {
    }

    /** The identifier of the program of a definition file, like {@code bloom-upsample.json} */
    public static synchronized int get(final String name) {
        return PROGRAMS.computeIfAbsent(name, key -> {
            try {
                final var definition = ShaderResources.programs().get(key);
                assertNotNull(definition, "The program " + key);

                final var program = glCreateProgram();
                for (final var shader : ShaderResources.shaders(definition)) {
                    final var source = ShaderResources.expand(shader);
                    final var id = glCreateShader(shader.contains("vertex") ? GL_VERTEX_SHADER : GL_FRAGMENT_SHADER);
                    glShaderSource(id, source.code());
                    glCompileShader(id);
                    assertEquals(GL_TRUE, glGetShaderi(id, GL_COMPILE_STATUS), shader + ": " + source.mapLog(glGetShaderInfoLog(id)));
                    glAttachShader(program, id);
                }

                glLinkProgram(program);
                assertEquals(GL_TRUE, glGetProgrami(program, GL_LINK_STATUS), glGetProgramInfoLog(program));

                return program;
            } catch (final Exception exception) {
                throw new IllegalStateException(exception);
            }
        });
    }

    /** Deletes the programs: the next call makes them again */
    public static synchronized void deleteAll() {
        PROGRAMS.values().forEach(program -> glDeleteProgram(program));
        PROGRAMS.clear();
    }
}
