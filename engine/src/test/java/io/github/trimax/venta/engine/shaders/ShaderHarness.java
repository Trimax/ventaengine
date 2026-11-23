package io.github.trimax.venta.engine.shaders;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.system.MemoryStack;

import io.github.trimax.venta.engine.model.common.shader.ShaderSource;
import io.github.trimax.venta.engine.utils.PreprocessingUtil;

/**
 * Calls the functions of the shaders on the GPU: a fragment shader made of the includes and the code of the test
 * writes a color into a float framebuffer of one pixel, which is read back.
 * <p>
 * The values of the uniforms are Integer, Float or float arrays (2 to 4 values), Floats for an array of floats and
 * Matrices for an array of 4x4 matrices.
 */
final class ShaderHarness {
    /** The values of an array of float uniforms */
    record Floats(float... values) {
    }

    /** The values of an array of mat4 uniforms: 16 numbers for every matrix, column by column */
    record Matrices(float... values) {
    }

    private static final String VERTEX = "post/vertex.glsl";
    private static final String TEST = "test.glsl";

    private final Map<String, Integer> programs = new HashMap<>();
    private final int framebuffer;
    private final int vertexArray;
    private final int texture;

    ShaderHarness() {
        vertexArray = glGenVertexArrays();
        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, 1, 1, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            throw new IllegalStateException("The framebuffer of the harness is incomplete");

        glViewport(0, 0, 1, 1);
    }

    /**
     * Runs the code in the main function of a fragment shader with the includes and the declarations before it.
     * The code writes the color into outputColor.
     *
     * @param includes     the files to include, like common/fog.glsl
     * @param declarations the declarations of the uniforms and the helper functions of the test
     * @param code         the code of the main function
     * @param uniforms     the values of the uniforms by their names
     * @return the color: red, green, blue and alpha
     */
    float[] run(final List<String> includes, final String declarations, final String code, final Map<String, Object> uniforms) {
        final var source = String.join("\n", "#version 330 core",
                String.join("\n", includes.stream().map(include -> "#include \"" + include + "\"").toList()),
                "out vec4 outputColor;", declarations, "void main() {", code, "}", "");
        final var program = programs.computeIfAbsent(source, ShaderHarness::build);

        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glViewport(0, 0, 1, 1);
        glDisable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        glUseProgram(program);
        uniforms.forEach((name, value) -> set(program, name, value));

        glBindVertexArray(vertexArray);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);

        final var color = new float[4];
        try (final var stack = MemoryStack.stackPush()) {
            final FloatBuffer pixel = stack.mallocFloat(4);
            glReadPixels(0, 0, 1, 1, GL_RGBA, GL_FLOAT, pixel);
            pixel.get(color);
        }

        glUseProgram(0);
        return color;
    }

    private static int build(final String source) {
        final var vertex = compile(GL_VERTEX_SHADER, VERTEX, ShaderResources.expand(VERTEX));
        final var fragment = compile(GL_FRAGMENT_SHADER, TEST, PreprocessingUtil.process(TEST,
                path -> path.equals(TEST) ? source : ShaderResources.load(path)));

        final var program = glCreateProgram();
        glAttachShader(program, vertex);
        glAttachShader(program, fragment);
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE)
            throw new IllegalStateException("The test program can't be linked: " + glGetProgramInfoLog(program));

        glDeleteShader(vertex);
        glDeleteShader(fragment);
        return program;
    }

    private static int compile(final int type, final String name, final ShaderSource source) {
        final var shader = glCreateShader(type);
        glShaderSource(shader, source.code());
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
            throw new IllegalStateException(name + " can't be compiled: " + source.mapLog(glGetShaderInfoLog(shader)) + "\n" + source.code());

        return shader;
    }

    private static void set(final int program, final String name, final Object value) {
        final var location = glGetUniformLocation(program, name);
        if (location < 0)
            throw new IllegalArgumentException("The test program has no uniform " + name);

        switch (value) {
            case Integer number -> glUniform1i(location, number);
            case Float number -> glUniform1f(location, number);
            case float[] numbers when numbers.length == 2 -> glUniform2fv(location, numbers);
            case float[] numbers when numbers.length == 3 -> glUniform3fv(location, numbers);
            case float[] numbers when numbers.length == 4 -> glUniform4fv(location, numbers);
            case Floats floats -> glUniform1fv(location, floats.values());
            case Matrices matrices -> glUniformMatrix4fv(location, false, matrices.values());
            default -> throw new IllegalArgumentException("Unsupported value of " + name + ": " + value);
        }
    }
}
