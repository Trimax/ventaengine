package com.venta.engine.renderers;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.*;
import static org.lwjgl.opengl.GL20C.glUniform1i;

import com.venta.engine.annotations.Component;
import com.venta.engine.enums.TextureType;
import com.venta.engine.exceptions.MaterialRenderingException;
import com.venta.engine.managers.MaterialManager;
import com.venta.engine.model.view.MaterialView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class MaterialRenderer extends AbstractRenderer<MaterialManager.MaterialEntity, MaterialView, MaterialRenderer.MaterialRenderContext> {
    @Override
    public void render(final MaterialView material) {
        if (material == null)
            return;

        final var context = getContext();
        if (context == null)
            throw new MaterialRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        final var textureDiffuse = material.getTexture(TextureType.DIFFUSE);
        glUniform1i(context.useTextureDiffuse, textureDiffuse == null ? 0 : 1);
        if (textureDiffuse != null) {
            glActiveTexture(GL_TEXTURE0);

            glBindTexture(GL_TEXTURE_2D, textureDiffuse.entity.getIdAsInteger());
            glUniform1i(context.getTextureDiffuse(), 0);
        }

        final var textureHeight = material.getTexture(TextureType.HEIGHT);
        glUniform1i(context.useTextureHeight, textureHeight == null ? 0 : 1);
        if (textureHeight != null) {
            glActiveTexture(GL_TEXTURE1);

            glBindTexture(GL_TEXTURE_2D, textureHeight.entity.getIdAsInteger());
            glUniform1i(context.getTextureHeight(), 0);
        }
    }

    @SuperBuilder
    @Getter(AccessLevel.PACKAGE)
    static final class MaterialRenderContext extends AbstractRenderContext {
        private final int useTextureDiffuse;
        private final int useTextureHeight;

        private final int textureDiffuse;
        private final int textureHeight;

        @Override
        public void close() {
        }
    }
}
