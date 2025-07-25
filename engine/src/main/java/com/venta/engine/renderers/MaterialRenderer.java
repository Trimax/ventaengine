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
        setTexture(TextureType.Normal, material, context.useTextureNormalUniformID, context.textureNormalUniformID);
        setTexture(TextureType.AmbientOcclusion, material, context.useTextureAmbientOcclusionUniformID, context.textureAmbientOcclusionUniformID);
        setTexture(TextureType.Roughness, material, context.useTextureRoughnessUniformID, context.textureRoughnessUniformID);
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
        private int useTextureAmbientOcclusionUniformID;
        private int useTextureRoughnessUniformID;
        private int useTextureDiffuseUniformID;
        private int useTextureHeightUniformID;
        private int useTextureNormalUniformID;

        private int textureAmbientOcclusionUniformID;
        private int textureRoughnessUniformID;
        private int textureDiffuseUniformID;
        private int textureHeightUniformID;
        private int textureNormalUniformID;

        public MaterialRenderContext withTextureDiffuse(final int textureUniformID, final int useTextureUniformID) {
            this.useTextureDiffuseUniformID = useTextureUniformID;
            this.textureDiffuseUniformID = textureUniformID;
            return this;
        }

        public MaterialRenderContext withTextureHeight(final int textureUniformID, final int useTextureHeightUniformID) {
            this.useTextureHeightUniformID = useTextureHeightUniformID;
            this.textureHeightUniformID = textureUniformID;
            return this;
        }

        public MaterialRenderContext withTextureNormal(final int textureUniformID, final int useTextureUniformID) {
            this.useTextureNormalUniformID = useTextureUniformID;
            this.textureNormalUniformID = textureUniformID;
            return this;
        }

        public MaterialRenderContext withTextureAmbientOcclusion(final int textureUniformID, final int useTextureUniformID) {
            this.useTextureAmbientOcclusionUniformID = useTextureUniformID;
            this.textureAmbientOcclusionUniformID = textureUniformID;
            return this;
        }

        public MaterialRenderContext withRoughness(final int textureUniformID, final int useTextureUniformID) {
            this.useTextureRoughnessUniformID = useTextureUniformID;
            this.textureRoughnessUniformID = textureUniformID;
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
