package io.github.trimax.venta.engine.renderers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.binders.MaterialBinder;
import io.github.trimax.venta.engine.model.entity.MeshEntity;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
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
final class MeshRenderer extends AbstractRenderer<MeshEntity, MeshRenderer.MeshRenderContext, ObjectRenderer.ObjectRenderContext> {
    private final MaterialBinder materialBinder;

    @Override
    protected MeshRenderContext createContext() {
        return new MeshRenderContext();
    }

    @Override
    public void render(final MeshEntity object) {
        glBindVertexArray(object.getVertexArrayObject().getData());
        materialBinder.bind(getContext().getProgram(), object.getMaterial());

        if (object.getFacetsCount() > 0) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, object.getFacetsBuffer().getData());
            glDrawElements(GL_TRIANGLES, object.getFacetsCount(), GL_UNSIGNED_INT, 0);
        }

        if (object.getEdgesCount() > 0) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, object.getEdgesBuffer().getData());
            glDrawElements(GL_LINES, object.getEdgesCount(), GL_UNSIGNED_INT, 0);
        }

        glBindVertexArray(0);
    }

    @Getter(AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    static final class MeshRenderContext extends AbstractRenderContext<ObjectRenderer.ObjectRenderContext> {
        private ProgramEntity program;

        public MeshRenderContext withProgram(final ProgramEntity program) {
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
