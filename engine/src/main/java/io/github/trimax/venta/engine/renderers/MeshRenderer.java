package io.github.trimax.venta.engine.renderers;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20C.glBindBuffer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.MaterialBinder;
import io.github.trimax.venta.engine.exceptions.ObjectRenderingException;
import io.github.trimax.venta.engine.managers.MeshManager;
import io.github.trimax.venta.engine.managers.ProgramManager;
import io.github.trimax.venta.engine.model.view.MeshView;
import io.github.trimax.venta.engine.model.view.ProgramView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class MeshRenderer extends AbstractRenderer<MeshView, MeshRenderer.MeshRenderContext, ObjectRenderer.ObjectRenderContext> {
    private final ProgramManager.ProgramAccessor programAccessor;
    private final MeshManager.MeshAccessor meshAccessor;
    private final MaterialBinder materialBinder;

    @Override
    protected MeshRenderContext createContext() {
        return new MeshRenderContext();
    }

    @Override
    public void render(final MeshView object) {
        final var context = getContext();
        if (context == null)
            throw new ObjectRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        render(meshAccessor.get(object.getID()), programAccessor.get(context.getProgram()));
    }

    private void render(final MeshManager.MeshEntity object, final ProgramManager.ProgramEntity program) {
        glBindVertexArray(object.getVertexArrayObjectID());
        materialBinder.bind(program, object.getMaterial());

        if (object.getFacetsCount() > 0) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, object.getFacetsBufferID());
            glDrawElements(GL_TRIANGLES, object.getFacetsCount(), GL_UNSIGNED_INT, 0);
        }

        if (object.getEdgesCount() > 0) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, object.getEdgesBufferID());
            glDrawElements(GL_LINES, object.getEdgesCount(), GL_UNSIGNED_INT, 0);
        }

        glBindVertexArray(0);
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    static final class MeshRenderContext extends AbstractRenderContext<ObjectRenderer.ObjectRenderContext> {
        private ProgramView program;

        public MeshRenderContext withProgram(final ProgramView program) {
            this.program = program;
            return this;
        }

        @Override
        public void close() {
            program = null;
        }

        @Override
        public void destroy() {
        }
    }
}
