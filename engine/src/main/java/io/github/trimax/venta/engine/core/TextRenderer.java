package io.github.trimax.venta.engine.core;

import static io.github.trimax.venta.engine.definitions.Definitions.*;
import static org.lwjgl.opengl.GL33C.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TextRenderer {


    private final int atlasesCount;
    private final int[] textureIds;
    private final STBTTBakedChar.Buffer[] charBuffers;

    private final int vao;
    private final int vbo;
    private final int shaderProgram;



    @SneakyThrows
    public TextRenderer(final String fontResourceName) {
        final var fontFile = Path.of(getClass().getResource(fontResourceName).toURI());
        ByteBuffer fontBuffer = ioResourceToByteBuffer(fontFile);

        // Support BMP Unicode (0..0xFFFF)
        atlasesCount = (65536 + FONT_ATLAS_CHARACTERS_COUNT - 1) / FONT_ATLAS_CHARACTERS_COUNT;

        textureIds = new int[atlasesCount];
        charBuffers = new STBTTBakedChar.Buffer[atlasesCount];

        for (int i = 0; i < atlasesCount; i++) {
            ByteBuffer bitmap = BufferUtils.createByteBuffer(FONT_ATLAS_WIDTH * FONT_ATLAS_HEIGHT);
            charBuffers[i] = STBTTBakedChar.malloc(FONT_ATLAS_CHARACTERS_COUNT);

            int firstChar = i * FONT_ATLAS_CHARACTERS_COUNT;

            int result = STBTruetype.stbtt_BakeFontBitmap(fontBuffer, FONT_HEIGHT, bitmap, FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT, firstChar, charBuffers[i]);
            if (result <= 0)
                throw new RuntimeException("Failed to bake font bitmap atlas " + i);

            textureIds[i] = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureIds[i]);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        }

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        shaderProgram = createShader();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // layout(location=0): vec2 aPos; layout(location=1): vec2 textureCoordinates;
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
        float penY = y - FONT_HEIGHT * scale;

        // Iterate through each character in the text
        for (int i = 0; i < text.length(); i++) {
            final var codepoint = text.codePointAt(i);

            if (Character.isHighSurrogate(text.charAt(i)) && i + 1 < text.length() && Character.isLowSurrogate(text.charAt(i + 1)))
                i++;

            final var atlasIndex = codepoint / FONT_ATLAS_CHARACTERS_COUNT;
            final var charIndex = codepoint % FONT_ATLAS_CHARACTERS_COUNT;

            if (atlasIndex < 0 || atlasIndex >= atlasesCount)
                continue;

            final var backedCharacter = charBuffers[atlasIndex].get(charIndex);

            final var x0 = penX + backedCharacter.xoff() * scale;
            final var x1 = x0 + (backedCharacter.x1() - backedCharacter.x0()) * scale;

            final var y0 = penY - (backedCharacter.y1() - backedCharacter.y0()) * scale - backedCharacter.yoff() * scale;
            final var y1 = y0 + (backedCharacter.y1() - backedCharacter.y0()) * scale;

            final var s0 = backedCharacter.x0() / (float) FONT_ATLAS_WIDTH;
            final var t0 = backedCharacter.y0() / (float) FONT_ATLAS_HEIGHT;
            final var s1 = backedCharacter.x1() / (float) FONT_ATLAS_WIDTH;
            final var t1 = backedCharacter.y1() / (float) FONT_ATLAS_HEIGHT;

            final var vertices = BufferUtils.createFloatBuffer(6 * 4);

            vertices.put(new float[]{
                    x0, y0, s0, t1,
                    x1, y0, s1, t1,
                    x1, y1, s1, t0,

                    x0, y0, s0, t1,
                    x1, y1, s1, t0,
                    x0, y1, s0, t0
            });
            vertices.flip();

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureIds[atlasIndex]);

            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);

            // Position
            final var posLoc = glGetUniformLocation(shaderProgram, "position");
            glUniform2f(posLoc, 0f, 0f);


            // Scale
            final var scaleLoc = glGetUniformLocation(shaderProgram, "scale");
            glUniform1f(scaleLoc, 1f);

            glDrawArrays(GL_TRIANGLES, 0, 6);

            penX += backedCharacter.xadvance() * scale;
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
                layout(location = 0) in vec2 vertexPosition;
                layout(location = 1) in vec2 textureCoordinates;

                out vec2 vertexTextureCoordinates;

                uniform vec2 position;
                uniform float scale;

                void main() {
                    // Coordinates in [-1;1]
                    vec2 pos = vertexPosition * scale + position;
                    gl_Position = vec4(pos.x, pos.y, 0.0, 1.0);
                    vertexTextureCoordinates = textureCoordinates;
                }
                """);
        glCompileShader(vs);
        if (glGetShaderi(vs, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException("Vertex shader error: " + glGetShaderInfoLog(vs));

        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs, """
                #version 330 core
                in vec2 vertexTextureCoordinates;
                out vec4 FragColor;

                uniform sampler2D textureDiffuse;

                void main() {
                    FragColor = vec4(0.2, 0.8, 0.2, texture(textureDiffuse, vertexTextureCoordinates).r);
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
        int fontTexLoc = glGetUniformLocation(program, "textureDiffuse");
        glUniform1i(fontTexLoc, 0);
        glUseProgram(0);

        return program;
    }

    private static ByteBuffer ioResourceToByteBuffer(Path path) throws IOException {
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
