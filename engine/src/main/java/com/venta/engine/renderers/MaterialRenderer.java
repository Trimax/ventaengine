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

@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class MaterialRenderer extends AbstractRenderer<MaterialManager.MaterialEntity, MaterialView, MaterialRenderer.MaterialRenderContext> {
    @Override
    protected MaterialRenderContext createContext() {
        return new MaterialRenderContext();
    }

    @Override
    public void render(final MaterialView material) {
        if (material == null)
            return;

        final var context = getContext();
        if (context == null)
            throw new MaterialRenderingException("RenderContext is not set. Did you forget to call withContext()?");

        final var textureDiffuse = material.getTexture(TextureType.DIFFUSE);
        glUniform1i(context.useTextureDiffuseUniformID, textureDiffuse == null ? 0 : 1);
        if (textureDiffuse != null) {
            glActiveTexture(GL_TEXTURE0);

            glBindTexture(GL_TEXTURE_2D, textureDiffuse.entity.getIdAsInteger());
            glUniform1i(context.textureDiffuseUniformID, 0);
        }

        final var textureHeight = material.getTexture(TextureType.HEIGHT);
        glUniform1i(context.useTextureHeightUniformID, textureHeight == null ? 0 : 1);
        if (textureHeight != null) {
            glActiveTexture(GL_TEXTURE1);

            glBindTexture(GL_TEXTURE_2D, textureHeight.entity.getIdAsInteger());
            glUniform1i(context.textureHeightUniformID, 0);
        }
    }

    static final class MaterialRenderContext extends AbstractRenderContext {
        private int useTextureDiffuseUniformID;
        private int useTextureHeightUniformID;

        private int textureDiffuseUniformID;
        private int textureHeightUniformID;

        public MaterialRenderContext withTextureDiffuse(final int textureDiffuseUniformID, final int useTextureDiffuseUniformID) {
            this.textureDiffuseUniformID = textureDiffuseUniformID;
            this.useTextureDiffuseUniformID = useTextureDiffuseUniformID;
            return this;
        }

        public MaterialRenderContext withTextureHeight(final int useTextureHeightUniformID, final int textureHeightUniformID) {
            this.useTextureHeightUniformID = useTextureHeightUniformID;
            this.textureHeightUniformID = textureHeightUniformID;
            return this;
        }

        @Override
        public void close() {
        }

        @Override
        public void destroy() {
        }
    }
}
