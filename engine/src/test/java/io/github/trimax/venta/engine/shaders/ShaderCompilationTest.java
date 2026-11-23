package io.github.trimax.venta.engine.shaders;

import static io.github.trimax.venta.engine.shaders.ShaderResources.expand;
import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL20C.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.common.shader.ShaderSource;

/**
 * Compiles and links the programs of the engine with the driver, so a mistake of a shader or of a common file (that is
 * used by many shaders) is found by a test and not by an example that doesn't start.
 */
final class ShaderCompilationTest {
    @BeforeAll
    static void context() {
        GLTestContext.require();
    }

    @Test
    void testEveryProgramIsCompiledAndLinked() throws Exception {
        final List<String> problems = new ArrayList<>();

        for (final var entry : ShaderResources.programs().entrySet()) {
            final var vertex = compile(GL_VERTEX_SHADER, entry.getValue().shaderVertex(), problems);
            final var fragment = compile(GL_FRAGMENT_SHADER, entry.getValue().shaderFragment(), problems);
            if (vertex == 0 || fragment == 0)
                continue;

            final var program = glCreateProgram();
            glAttachShader(program, vertex);
            glAttachShader(program, fragment);
            glLinkProgram(program);
            if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE)
                problems.add(entry.getKey() + " can't be linked: " + glGetProgramInfoLog(program));
            else
                assertTrue(glGetProgrami(program, GL_ACTIVE_UNIFORMS) >= 0, "Active uniforms of " + entry.getKey());

            glDeleteProgram(program);
            glDeleteShader(vertex);
            glDeleteShader(fragment);
        }

        assertTrue(problems.isEmpty(), "Problems of the shaders:\n" + String.join("\n", problems));
    }

    @Test
    void testCompilerLogPointsToTheFileOfAMistake() {
        final var source = expand("post/fragment.glsl");

        assertTrue(mistakeLog(source, "vec3 low = color * 12.92;", "vec3 low = color * undefinedValue;").lines()
                .anyMatch(line -> line.contains("common/color.glsl")), "A mistake of an included file must be in the log with its name");
        assertTrue(mistakeLog(source, "outputColor = vec4(", "outputColor = vec4(undefinedValue, ").contains("post/fragment.glsl"),
                "A mistake of the shader must be in the log with the name of the shader");
    }

    /** The log of the compiler, mapped to the files, for the shader with the text replaced */
    private static String mistakeLog(final ShaderSource source, final String text, final String replacement) {
        assertTrue(source.code().contains(text), "The code to break must be in the shader: " + text);

        final var shader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(shader, source.code().replace(text, replacement));
        glCompileShader(shader);
        assertEquals(GL_FALSE, glGetShaderi(shader, GL_COMPILE_STATUS), "The broken shader must not compile");

        final var log = source.mapLog(glGetShaderInfoLog(shader));
        glDeleteShader(shader);
        return log;
    }

    /** The shader or 0 with the log of the compiler in the problems */
    private static int compile(final int type, final String path, final List<String> problems) {
        final var source = expand(path);
        final var shader = glCreateShader(type);
        glShaderSource(shader, source.code());
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            problems.add(path + " can't be compiled: " + source.mapLog(glGetShaderInfoLog(shader)));
            glDeleteShader(shader);
            return 0;
        }

        return shader;
    }
}
