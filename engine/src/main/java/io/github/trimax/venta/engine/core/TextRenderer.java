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

    private static final int CHARS_PER_ATLAS = 256;
    private static final int FONT_HEIGHT = 32;
    private static final int BITMAP_W = 2048;
    private static final int BITMAP_H = 2048;

    private final int atlasesCount;
    private final int[] textureIds;
    private final STBTTBakedChar.Buffer[] charBuffers;

    private final int vao;
    private final int vbo;
    private final int shaderProgram;

    @SneakyThrows
    public TextRenderer(String fontResourceName) {
        final var fontFile = Path.of(getClass().getResource(fontResourceName).toURI());
        ByteBuffer fontBuffer = ioResourceToByteBuffer(fontFile, 160 * 1024);

        // Поддержка BMP Unicode (0..0xFFFF)
        int totalChars = 0x10000; // 65536
        atlasesCount = (totalChars + CHARS_PER_ATLAS - 1) / CHARS_PER_ATLAS;

        textureIds = new int[atlasesCount];
        charBuffers = new STBTTBakedChar.Buffer[atlasesCount];

        for (int i = 0; i < atlasesCount; i++) {
            ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
            charBuffers[i] = STBTTBakedChar.malloc(CHARS_PER_ATLAS);

            int firstChar = i * CHARS_PER_ATLAS;

            int result = STBTruetype.stbtt_BakeFontBitmap(fontBuffer, FONT_HEIGHT, bitmap, BITMAP_W, BITMAP_H, firstChar, charBuffers[i]);
            if (result <= 0)
                throw new RuntimeException("Failed to bake font bitmap atlas " + i);

            textureIds[i] = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureIds[i]);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, BITMAP_W, BITMAP_H, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        }

        // Создаем VAO, VBO и шейдеры
        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        shaderProgram = createShader();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // layout(location=0): vec2 aPos; layout(location=1): vec2 aTexCoord;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void renderText(String text, float x, float y, float scale) {
        glUseProgram(shaderProgram);
        glBindVertexArray(vao);

        float penX = x;
        float penY = y;

        // Проходим по символам и рисуем пачками по атласам
        // Для оптимизации можно группировать по атласам, но для простоты — один за другим
        for (int i = 0; i < text.length(); i++) {
            int codepoint = text.codePointAt(i);

            // Если суррогатный пара — пропускаем второй char (т.к. код по UTF-16 занимает 2 char)
            if (Character.isHighSurrogate(text.charAt(i)) && i + 1 < text.length() && Character.isLowSurrogate(text.charAt(i + 1))) {
                i++;
            }

            int atlasIndex = codepoint / CHARS_PER_ATLAS;
            int charIndex = codepoint % CHARS_PER_ATLAS;

            if (atlasIndex < 0 || atlasIndex >= atlasesCount)
                continue; // Символ вне диапазона

            STBTTBakedChar g = charBuffers[atlasIndex].get(charIndex);

            float x0 = penX + g.xoff() * scale;
            float y0 = penY + g.yoff() * scale;
            float x1 = x0 + (g.x1() - g.x0()) * scale;
            float y1 = y0 + (g.y1() - g.y0()) * scale;

            float s0 = g.x0() / (float) BITMAP_W;
            float t0 = g.y0() / (float) BITMAP_H;
            float s1 = g.x1() / (float) BITMAP_W;
            float t1 = g.y1() / (float) BITMAP_H;

            FloatBuffer vertices = BufferUtils.createFloatBuffer(6 * 4); // 6 вершин по 4 атрибута

            vertices.put(new float[]{
                    x0, y0, s0, t0,
                    x1, y0, s1, t0,
                    x1, y1, s1, t1,

                    x0, y0, s0, t0,
                    x1, y1, s1, t1,
                    x0, y1, s0, t1
            });
            vertices.flip();

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureIds[atlasIndex]);

            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);

            // Передаем позицию и масштаб в шейдер
            int posLoc = glGetUniformLocation(shaderProgram, "uPos");
            glUniform2f(posLoc, 0f, 0f);

            int scaleLoc = glGetUniformLocation(shaderProgram, "uScale");
            glUniform1f(scaleLoc, 1f);

            glDrawArrays(GL_TRIANGLES, 0, 6);

            penX += g.xadvance() * scale;
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
                    // Координаты в [-1;1], масштабируем и сдвигаем
                    vec2 pos = aPos * uScale + uPos;
                    gl_Position = vec4(pos.x, pos.y, 0.0, 1.0);
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
                    FragColor = vec4(1.0, 0.0, 0.0, alpha);
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
        for (int texId : textureIds)
            glDeleteTextures(texId);
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        glDeleteProgram(shaderProgram);
        for (STBTTBakedChar.Buffer buf : charBuffers)
            buf.free();
    }
}
