package io.github.trimax.venta.engine.binders;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13C.glActiveTexture;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.enums.TextureUnit;
import io.github.trimax.venta.engine.model.entity.implementation.CubemapEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import io.github.trimax.venta.engine.registries.implementation.TextureRegistryImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextureBinder extends AbstractBinder {
    private final TextureRegistryImplementation textureRegistry;

    public void bind(final ProgramEntityImplementation program, final CubemapEntityImplementation skybox) {
        bind(program.getUniformID(ShaderUniform.UseTextureSkybox), skybox != null);

        glActiveTexture(TextureUnit.Skybox.getLocationID());
        bind(program.getUniformID(ShaderUniform.TextureSkybox), TextureUnit.Skybox.getId());

        glBindTexture(GL_TEXTURE_CUBE_MAP, skybox != null ? skybox.getInternalID() : textureRegistry.getDefaultTexture().getInternalID());
    }

    public void bind(final ProgramEntityImplementation program, final TextureEntityImplementation texture) {
        bind(TextureUnit.Debug, texture, program.getUniformID(ShaderUniform.UseTextureDebug), program.getUniformID(ShaderUniform.TextureDebug));
    }

    public void bind(final TextureType type, final ProgramEntityImplementation program, final TextureEntityImplementation texture) {
        bind(type.getUnit(), texture, program.getUniformID(type.getUseTextureUniform()), program.getUniformID(type.getTextureUniform()));
    }

    private void bind(final TextureUnit unit, final TextureEntityImplementation texture, final int useTextureUniformID, final int textureUniformID) {
        bind(useTextureUniformID, texture != null);

        glActiveTexture(unit.getLocationID());
        bind(textureUniformID, unit.getId());

        glBindTexture(GL_TEXTURE_2D, texture != null ? texture.getInternalID() : textureRegistry.getDefaultTexture().getInternalID());
    }
}
