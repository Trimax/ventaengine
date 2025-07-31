package io.github.trimax.venta.engine.renderers;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.ConsoleManager;
import io.github.trimax.venta.engine.managers.ProgramManager;
import io.github.trimax.venta.engine.model.view.ConsoleView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleRenderer extends AbstractRenderer<ConsoleView, ConsoleRenderer.ConsoleRenderContext, WindowRenderer.WindowRenderContext> {
    private final ProgramManager.ProgramAccessor programAccessor;
    private final ProgramManager programManager;

    @Override
    protected ConsoleRenderContext createContext() {
        return new ConsoleRenderContext();
    }

    @Override
    void render(final ConsoleView console) {
        if (console instanceof ConsoleManager.ConsoleEntity entity)
            render(entity, programAccessor.get(programManager.load("console")));
    }

    private void render(final ConsoleManager.ConsoleEntity console, final ProgramManager.ProgramEntity program) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glUseProgram(program.getInternalID());
        glBindVertexArray(console.getVertexArrayObjectID());

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

        glDisable(GL_BLEND);
        glBindVertexArray(0);
        glUseProgram(0);

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    @Getter(AccessLevel.PACKAGE)
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class ConsoleRenderContext extends AbstractRenderContext<WindowRenderer.WindowRenderContext> {
        @Override
        public void close() {
        }

        @Override
        public void destroy() {
        }
    }
}
