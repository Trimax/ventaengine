package io.github.trimax.venta.engine.renderers.state;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.TextBinder;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.model.states.TextState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector2f;

import static io.github.trimax.venta.engine.definitions.Definitions.*;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL20C.glUseProgram;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextStateRenderer extends AbstractStateRenderer<TextState, TextStateRenderer.TextRenderContext, ConsoleStateRenderer.ConsoleRenderContext> {
    private final GeometryHelper geometryHelper;
    private final TextBinder textBinder;

    @Override
    protected TextRenderContext createContext() {
        return new TextRenderContext();
    }

    @Override
    public void render(final TextState state) {
        glUseProgram(state.getProgram().getInternalID());

        float penX = getContext().linePosition.x;
        final float penY = getContext().linePosition.y;

        // Iterate through each character in the text
        final var message = getContext().message;
        final var text = message.text();
        final var font = state.getFont();

        for (int i = 0; i < text.length(); i++) {
            final var codepoint = text.codePointAt(i);

            if (Character.isHighSurrogate(text.charAt(i)) && i + 1 < text.length() && Character.isLowSurrogate(text.charAt(i + 1)))
                i++;

            final var atlasIndex = codepoint / FONT_ATLAS_CHARACTERS_COUNT;
            final var charIndex = codepoint % FONT_ATLAS_CHARACTERS_COUNT;

            if (atlasIndex < 0 || atlasIndex >= font.getAtlases().size())
                continue;

            final var backedCharacter = font.getAtlases().get(atlasIndex).getBuffer().get(charIndex);

            /* Compute character size in atlas  */
            final var charWidth = backedCharacter.x1() - backedCharacter.x0();
            final var charHeight = backedCharacter.y1() - backedCharacter.y0();

            /* Compute texture coordinates */
            final var s0 = backedCharacter.x0() / (float) FONT_ATLAS_WIDTH;
            final var t0 = backedCharacter.y0() / (float) FONT_ATLAS_HEIGHT;
            final var s1 = backedCharacter.x1() / (float) FONT_ATLAS_WIDTH;
            final var t1 = backedCharacter.y1() / (float) FONT_ATLAS_HEIGHT;

            /* Compute character position in screen space */
            final float x0px = penX + getContext().scale * backedCharacter.xoff();
            final float y0px = penY + getContext().scale * backedCharacter.yoff() * Definitions.CONSOLE_LINE_HEIGHT_SCALE;
            final float x1px = x0px + getContext().scale * charWidth;
            final float y1px = y0px + getContext().scale * charHeight * Definitions.CONSOLE_LINE_HEIGHT_SCALE;

            /* Translating screen coordinates into NDC */
            final float x0NDC = (x0px / getContext().windowSize.x) * 2f - 1f;
            final float x1NDC = (x1px / getContext().windowSize.x) * 2f - 1f;
            final float y0NDC = 1f - (y0px / getContext().windowSize.y) * 2f;
            final float y1NDC = 1f - (y1px / getContext().windowSize.y) * 2f;

            textBinder.bindColor(state.getProgram(), getContext().getMessage().type().getColor());
            textBinder.bindPosition(state.getProgram(), x0NDC, y0NDC, x1NDC, y1NDC);
            textBinder.bindTextureCoordinates(state.getProgram(), s0, t0, s1, t1);

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, font.getAtlases().get(atlasIndex).getTexture().getInternalID());

            geometryHelper.render(state.getGeometry());

            penX += getContext().scale * backedCharacter.xadvance();
        }

        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class TextRenderContext extends AbstractRenderContext<ConsoleStateRenderer.ConsoleRenderContext> {
        private final Vector2f windowSize = new Vector2f();
        private final Vector2f linePosition = new Vector2f();

        private ConsoleController.ConsoleMessage message;
        private float scale;

        public TextRenderContext withWindow(final int width, final int height) {
            windowSize.set(width, height);

            return this;
        }

        public TextRenderContext withPosition(final float x, final float y) {
            linePosition.set(x, y);

            return this;
        }

        public TextRenderContext withScale(final float scale) {
            this.scale = scale;

            return this;
        }

        public TextRenderContext withText(final ConsoleController.ConsoleMessage message) {
            this.message = message;
            return this;
        }

        @Override
        public void close() {
            this.scale = 1.f;
            this.message = null;
            this.windowSize.set(0);
            this.linePosition.set(0);
        }

        @Override
        public void destroy() {
        }
    }
}
