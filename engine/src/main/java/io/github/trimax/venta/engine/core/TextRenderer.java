package io.github.trimax.venta.engine.core;

import static org.lwjgl.opengl.GL33C.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;

import lombok.SneakyThrows;

public class TextRenderer {

    private static final int BITMAP_W = 512;
    private static final int BITMAP_H = 512;
    private static final int FONT_HEIGHT = 32;
    private static final int FIRST_CHAR = 32;
    private static final int NUM_CHARS = 96;

    private final int textureId;
    private final STBTTBakedChar.Buffer charData;
    private final int vao;
    private final int vbo;
    private final int shaderProgram;

    @SneakyThrows
    public TextRenderer(String name) {
        final var fontFile = Path.of(getClass().getResource(name).toURI());
        ByteBuffer fontBuffer = ioResourceToByteBuffer(fontFile, 160 * 1024);

        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
        charData = STBTTBakedChar.malloc(NUM_CHARS);

        int result = STBTruetype.stbtt_BakeFontBitmap(fontBuffer, FONT_HEIGHT, bitmap, BITMAP_W, BITMAP_H, FIRST_CHAR, charData);
        if (result <= 0) throw new RuntimeException("Failed to bake font bitmap");

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, BITMAP_W, BITMAP_H, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        shaderProgram = createShader();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void renderText(String text, float x, float y, float scale) {
        if (text == null || text.isEmpty()) return;

        glUseProgram(shaderProgram);
        glBindVertexArray(vao);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);

        int posLoc = glGetUniformLocation(shaderProgram, "uPos");
        glUniform2f(posLoc, x, y);

        int scaleLoc = glGetUniformLocation(shaderProgram, "uScale");
        glUniform1f(scaleLoc, scale);

        FloatBuffer vertices = BufferUtils.createFloatBuffer(text.length() * 6 * 4);

        float cursorX = 0.0f;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < FIRST_CHAR || c >= FIRST_CHAR + NUM_CHARS) continue;

            STBTTBakedChar g = charData.get(c - FIRST_CHAR);

            float x0 = cursorX + g.xoff();
            float y0 = -g.yoff();
            float x1 = x0 + (g.x1() - g.x0());
            float y1 = y0 - (g.y1() - g.y0());

            float s0 = g.x0() / (float) BITMAP_W;
            float t0 = g.y0() / (float) BITMAP_H;
            float s1 = g.x1() / (float) BITMAP_W;
            float t1 = g.y1() / (float) BITMAP_H;

            vertices.put(new float[]{
                    x0, y0, s0, t0,
                    x1, y0, s1, t0,
                    x1, y1, s1, t1,
                    x0, y0, s0, t0,
                    x1, y1, s1, t1,
                    x0, y1, s0, t1,
            });

            cursorX += g.xadvance();
        }

        vertices.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);

        int vertexCount = vertices.limit() / 4;
        if (vertexCount > 0) {
            glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
    }

    private int createShader() {
        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs, """
            #version 330 core
            layout(location = 0) in vec2 aPos;
            layout(location = 1) in vec2 aTexCoord;

            out vec2 TexCoord;

            uniform vec2 uPos;
            uniform float uScale;

            void main() {
                vec2 pos = (aPos * uScale + uPos);
                gl_Position = vec4(pos.xy, 0.0, 1.0);
                TexCoord = aTexCoord;
            }
            """);
        glCompileShader(vs);
        if (glGetShaderi(vs, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException("Vertex shader error: " + glGetShaderInfoLog(vs));

        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs, """
            #version 330 core
            in vec2 TexCoord;
            out vec4 FragColor;

            uniform sampler2D fontTexture;

            void main() {
                float alpha = texture(fontTexture, TexCoord).r;
                FragColor = vec4(1.0, 1.0, 1.0, alpha);
            }
            """);
        glCompileShader(fs);
        if (glGetShaderi(fs, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException("Fragment shader error: " + glGetShaderInfoLog(fs));

        int program = glCreateProgram();
        glAttachShader(program, vs);
        glAttachShader(program, fs);
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE)
            throw new RuntimeException("Program linking error: " + glGetProgramInfoLog(program));

        glDeleteShader(vs);
        glDeleteShader(fs);

        glUseProgram(program);
        int fontTexLoc = glGetUniformLocation(program, "fontTexture");
        glUniform1i(fontTexLoc, 0);
        glUseProgram(0);

        return program;
    }

    private static ByteBuffer ioResourceToByteBuffer(Path path, int bufferSize) throws IOException {
        ByteBuffer buffer;
        try (FileChannel fc = FileChannel.open(path, StandardOpenOption.READ)) {
            buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
            while (fc.read(buffer) != -1) ;
        }
        buffer.flip();
        return buffer;
    }

    public void cleanup() {
        glDeleteTextures(textureId);
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        glDeleteProgram(shaderProgram);
        charData.free();
    }
}
