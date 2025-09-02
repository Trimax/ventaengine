package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.TextureUnit;
import io.github.trimax.venta.engine.model.entity.implementation.CubemapEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
            glUniform1i(program.getUniformID(ShaderUniform.UseTextureSkybox), 0);
            return;
        }

        glBindTexture(GL_TEXTURE_CUBE_MAP, skybox.getInternalID());
        glUniform1i(program.getUniformID(ShaderUniform.TextureSkybox), TextureUnit.Skybox.getId());
        glUniform1i(program.getUniformID(ShaderUniform.UseTextureSkybox), 1);
    }
}
