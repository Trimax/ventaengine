package io.github.trimax.venta.engine.renderers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.ConsoleManagerImplementation;
import io.github.trimax.venta.engine.model.entities.ConsoleItemEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static io.github.trimax.venta.engine.definitions.Definitions.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleItemRenderer extends AbstractRenderer<ConsoleItemEntity, ConsoleItemRenderer.ConsoleItemRenderContext, ConsoleRenderer.ConsoleRenderContext> {
    @Override
    protected ConsoleItemRenderContext createContext() {
        return new ConsoleItemRenderContext();
    }

    @Override
    void render(final ConsoleItemEntity consoleItem) {
        glUseProgram(consoleItem.getProgram().getInternalID());
        glBindVertexArray(consoleItem.getVertexArrayObjectID());

        float penX = getContext().x;
        final float penY = getContext().y - FONT_HEIGHT * getContext().scale;

        // Iterate through each character in the text
        final var message = getContext().message;
        final var color = message.type().getColor();
        final var text = message.text();
        final var font = consoleItem.getFont();

        for (int i = 0; i < text.length(); i++) {
            final var codepoint = text.codePointAt(i);

            if (Character.isHighSurrogate(text.charAt(i)) && i + 1 < text.length() && Character.isLowSurrogate(text.charAt(i + 1)))
                i++;

            final var atlasIndex = codepoint / FONT_ATLAS_CHARACTERS_COUNT;
            final var charIndex = codepoint % FONT_ATLAS_CHARACTERS_COUNT;

            if (atlasIndex < 0 || atlasIndex >= font.getAtlases().size())
                continue;

            final var backedCharacter = font.getAtlases().get(atlasIndex).getBuffer().get(charIndex);

            final var x0 = penX + backedCharacter.xoff() * getContext().scale;
            final var x1 = x0 + (backedCharacter.x1() - backedCharacter.x0()) * getContext().scale;

            final var y0 = penY - (backedCharacter.y1() - backedCharacter.y0()) * getContext().scale - backedCharacter.yoff() * getContext().scale;
            final var y1 = y0 + (backedCharacter.y1() - backedCharacter.y0()) * getContext().scale;

            final var s0 = backedCharacter.x0() / (float) FONT_ATLAS_WIDTH;
            final var t0 = backedCharacter.y0() / (float) FONT_ATLAS_HEIGHT;
            final var s1 = backedCharacter.x1() / (float) FONT_ATLAS_WIDTH;
            final var t1 = backedCharacter.y1() / (float) FONT_ATLAS_HEIGHT;

            //vertices.rewind();
            getContext().vertices.put(new float[]{
                    x0, y0, s0, t1,
                    x1, y0, s1, t1,
                    x1, y1, s1, t0,

                    x0, y0, s0, t1,
                    x1, y1, s1, t0,
                    x0, y1, s0, t0
            });
            getContext().vertices.flip();

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, font.getAtlases().get(atlasIndex).getTexture().getInternalID());

            glBindBuffer(GL_ARRAY_BUFFER, consoleItem.getVerticesBufferID());
            glBufferData(GL_ARRAY_BUFFER, getContext().vertices, GL_DYNAMIC_DRAW);


            // TODO: Move to binder
            // Position
            glUniform2f(consoleItem.getProgram().getUniformID("position"), 0f, 0f);

            // Scale
            glUniform1f(consoleItem.getProgram().getUniformID("scale"), 1f);

            glUniform3f(consoleItem.getProgram().getUniformID("color"), color.x, color.y, color.z);

            glDrawArrays(GL_TRIANGLES, 0, 6);

            penX += backedCharacter.xadvance() * getContext().scale;
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class ConsoleItemRenderContext extends AbstractRenderContext<ConsoleRenderer.ConsoleRenderContext> {
        private final FloatBuffer vertices = BufferUtils.createFloatBuffer(6 * 4);

        private float x;
        private float y;
        private float scale;
        private ConsoleManagerImplementation.ConsoleMessage message;

        public ConsoleItemRenderContext withPosition(final float x, final float y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public ConsoleItemRenderContext withScale(final float scale) {
            this.scale = scale;
            return this;
        }

        public ConsoleItemRenderContext withText(final ConsoleManagerImplementation.ConsoleMessage message) {
            this.message = message;
            return this;
        }

        @Override
        public void close() {
            vertices.rewind();
            this.message = null;
            this.scale = 0.f;
            this.x = 0.f;
            this.y = 0.f;
        }

        @Override
        public void destroy() {

        }
    }
}
