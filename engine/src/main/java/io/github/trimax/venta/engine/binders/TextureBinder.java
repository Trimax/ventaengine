package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.enums.TextureUnit;
import io.github.trimax.venta.engine.model.entity.implementation.CubemapEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL20C.glUniform1i;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextureBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final CubemapEntityImplementation skybox) {
        bind(program.getUniformID(ShaderUniform.UseTextureSkybox), skybox != null);

        glActiveTexture(TextureUnit.Skybox.getLocationID());
        if (skybox == null) {
            glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
            return;
        }

        glBindTexture(GL_TEXTURE_CUBE_MAP, skybox.getInternalID());
        glUniform1i(program.getUniformID(ShaderUniform.TextureSkybox), TextureUnit.Skybox.getId());
    }

    public void bind(final TextureType type, final ProgramEntityImplementation program, final TextureEntityImplementation texture) {
        bind(type.getUnit(), texture, program.getUniformID(type.getUseTextureUniform()), program.getUniformID(type.getTextureUniform()));
    }

    private void bind(final TextureUnit unit, final TextureEntityImplementation texture, final int useTextureUniformID, final int textureUniformID) {
        glActiveTexture(unit.getLocationID());
        if (texture == null) {
            glBindTexture(GL_TEXTURE_2D, 0);
            glUniform1i(useTextureUniformID, 0);
            return;
        }

        glBindTexture(GL_TEXTURE_2D, texture.getInternalID());
        glUniform1i(textureUniformID, unit.getId());
        glUniform1i(useTextureUniformID, 1);
    }
}
