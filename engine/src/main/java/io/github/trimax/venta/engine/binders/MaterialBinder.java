package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.managers.implementation.ProgramManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.TextureManagerImplementation;
import io.github.trimax.venta.engine.model.view.MaterialView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL20C.glUniform1i;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaterialBinder extends AbstractBinder {
    private final TextureManagerImplementation textureManager;

    public void bind(final ProgramManagerImplementation.ProgramEntity program, final MaterialView material) {
        if (material == null)
            return;

        bind(TextureType.Diffuse, program, material, ShaderUniform.UseTextureDiffuseFlag, ShaderUniform.TextureDiffuse);
        bind(TextureType.Height, program, material, ShaderUniform.UseTextureHeight, ShaderUniform.TextureHeight);
        bind(TextureType.Normal, program, material, ShaderUniform.UseTextureNormal, ShaderUniform.TextureNormal);
        bind(TextureType.AmbientOcclusion, program, material, ShaderUniform.UseTextureAmbientOcclusion, ShaderUniform.TextureAmbientOcclusion);
        bind(TextureType.Roughness, program, material, ShaderUniform.UseTextureRoughness, ShaderUniform.TextureRoughness);
    }

    private void bind(final TextureType type, final ProgramManagerImplementation.ProgramEntity program,
                      final MaterialView material, final ShaderUniform useTextureUniform, final ShaderUniform textureUniform) {
        bind(type, textureManager.getEntity(material.getTexture(type).getID()), program.getUniformID(useTextureUniform), program.getUniformID(textureUniform));
    }

    private void bind(final TextureType type, final TextureManagerImplementation.TextureEntity texture, final int useTextureUniformID, final int textureUniformID) {
        glActiveTexture(type.getLocationID());
        if (texture == null) {
            glBindTexture(GL_TEXTURE_2D, 0);
            glUniform1i(useTextureUniformID, 0);
            return;
        }

        glBindTexture(GL_TEXTURE_2D, texture.getInternalID());
        glUniform1i(textureUniformID, type.getUnitID());
        glUniform1i(useTextureUniformID, 1);
    }
}
