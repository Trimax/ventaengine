package io.github.trimax.venta.engine.binders;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.glActiveTexture;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.TextureUnit;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElevationBinder extends AbstractBinder {

    public void bind(final ProgramEntityImplementation program, final TextureEntityImplementation heightmap, final float[] elevations, final float factor) {
        glActiveTexture(TextureUnit.Elevation.getLocationID());
        bind(program.getUniformID(ShaderUniform.TextureElevation), TextureUnit.Elevation.getId());
        glBindTexture(GL_TEXTURE_2D, heightmap != null ? heightmap.getInternalID() : 0);

        bind(program.getUniformID(ShaderUniform.Factor), factor);
        bind(program.getUniformID(ShaderUniform.Elevations), elevations);
    }
}
