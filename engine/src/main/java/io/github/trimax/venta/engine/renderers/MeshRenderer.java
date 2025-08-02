package io.github.trimax.venta.engine.renderers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.MaterialBinder;
import io.github.trimax.venta.engine.managers.implementation.MeshManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ProgramManagerImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20C.glBindBuffer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class MeshRenderer extends AbstractRenderer<MeshManagerImplementation.MeshEntity, MeshRenderer.MeshRenderContext, ObjectRenderer.ObjectRenderContext> {
    private final MaterialBinder materialBinder;

    @Override
    protected MeshRenderContext createContext() {
        return new MeshRenderContext();
    }

    @Override
    public void render(final MeshManagerImplementation.MeshEntity object) {
        glBindVertexArray(object.getVertexArrayObjectID());
        materialBinder.bind(getContext().getProgram(), object.getMaterial());

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
        private ProgramManagerImplementation.ProgramEntity program;

        public MeshRenderContext withProgram(final ProgramManagerImplementation.ProgramEntity program) {
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
