package com.venta.engine.renderers;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
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

        setTexture(TextureType.Diffuse, material, context.useTextureDiffuseUniformID, context.textureDiffuseUniformID);
        setTexture(TextureType.Height, material, context.useTextureHeightUniformID, context.textureHeightUniformID);
        setTexture(TextureType.Normal, material, context.useTextureHeightUniformID, context.textureHeightUniformID);
    }

    private void setTexture(final TextureType type, final MaterialView material, final int useTextureUniformID, final int textureUniformID) {
        final var texture = material.getTexture(type);
        glActiveTexture(type.getLocationID());
        if (texture == null) {
            glBindTexture(GL_TEXTURE_2D, 0);
            glUniform1i(useTextureUniformID, 0);
            return;
        }

        glBindTexture(GL_TEXTURE_2D, texture.entity.getIdAsInteger());
        glUniform1i(textureUniformID, type.getUnitID());
        glUniform1i(useTextureUniformID, 1);
    }

    static final class MaterialRenderContext extends AbstractRenderContext {
        private int useTextureDiffuseUniformID;
        private int useTextureHeightUniformID;
        private int useTextureNormalUniformID;

        private int textureDiffuseUniformID;
        private int textureHeightUniformID;
        private int textureNormalUniformID;

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

        public MaterialRenderContext withTextureNormal(final int useTextureNormalUniformID, final int textureNormalUniformID) {
            this.useTextureNormalUniformID = useTextureNormalUniformID;
            this.textureNormalUniformID = textureNormalUniformID;
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
