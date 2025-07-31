package io.github.trimax.venta.engine.console;

import static io.github.trimax.venta.engine.definitions.Definitions.*;
import static org.lwjgl.opengl.GL33C.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import io.github.trimax.venta.engine.managers.FontManager;
import io.github.trimax.venta.engine.managers.ProgramManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TextRenderer {
    private final ProgramManager.ProgramEntity program;
    private final FontManager.FontEntity font;

    private final FloatBuffer vertices = BufferUtils.createFloatBuffer(6 * 4);

    private final int vao;
    private final int vbo;

    @SneakyThrows
    public TextRenderer(final FontManager.FontEntity font, final ProgramManager.ProgramEntity program) {
        this.program = program;
        this.font = font;

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

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

    public void renderText(final String text, final float x, final float y, final float scale) {
        glUseProgram(program.getInternalID());
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

            if (atlasIndex < 0 || atlasIndex >= font.getAtlases().size())
                continue;

            final var backedCharacter = font.getAtlases().get(atlasIndex).getBuffer().get(charIndex);

            final var x0 = penX + backedCharacter.xoff() * scale;
            final var x1 = x0 + (backedCharacter.x1() - backedCharacter.x0()) * scale;

            final var y0 = penY - (backedCharacter.y1() - backedCharacter.y0()) * scale - backedCharacter.yoff() * scale;
            final var y1 = y0 + (backedCharacter.y1() - backedCharacter.y0()) * scale;

            final var s0 = backedCharacter.x0() / (float) FONT_ATLAS_WIDTH;
            final var t0 = backedCharacter.y0() / (float) FONT_ATLAS_HEIGHT;
            final var s1 = backedCharacter.x1() / (float) FONT_ATLAS_WIDTH;
            final var t1 = backedCharacter.y1() / (float) FONT_ATLAS_HEIGHT;

            vertices.rewind();
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
            glBindTexture(GL_TEXTURE_2D, font.getAtlases().get(atlasIndex).getTexture().getInternalID());

            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);

            // Position
            glUniform2f(program.getUniformID("position"), 0f, 0f);

            // Scale
            glUniform1f(program.getUniformID("scale"), 1f);

            glDrawArrays(GL_TRIANGLES, 0, 6);

            penX += backedCharacter.xadvance() * scale;
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
    }

    public void cleanup() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        vertices.clear();
    }
}
