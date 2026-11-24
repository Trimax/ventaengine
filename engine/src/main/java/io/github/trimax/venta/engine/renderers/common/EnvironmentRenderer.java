package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13C.glActiveTexture;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.TextureUnit;
import io.github.trimax.venta.engine.helpers.EnvironmentHelper;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.environment.EnvironmentMap;
import io.github.trimax.venta.engine.model.entity.implementation.CubemapEntityImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Keeps the light of the environment for the skybox of the scene: when the skybox changes, the maps of the image based
 * lighting are convolved from it (see {@link EnvironmentHelper}) and bound to their texture units, the shaders of the
 * objects and the terrain take them from there. A scene without a skybox has none.
 */
@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnvironmentRenderer {
    private final ProgramRegistryImplementation programRegistry;
    private final Memory memory;

    private EnvironmentHelper generator;
    private CubemapEntityImplementation skybox;
    private int brdfID;

    /** The maps for the skybox of the last frame, null without a skybox */
    @Getter
    private EnvironmentMap environment;

    /** Makes sure the environment belongs to the skybox: builds it when the skybox is new, drops it when there is none */
    public void prepare(final CubemapEntityImplementation skybox) {
        if (skybox == null) {
            dropEnvironment();
            return;
        }

        if (environment == null || skybox != this.skybox) {
            dropEnvironment();
            build(skybox);
        }

        glActiveTexture(TextureUnit.Irradiance.getLocationID());
        glBindTexture(GL_TEXTURE_CUBE_MAP, environment.irradianceID());
        glActiveTexture(TextureUnit.Prefilter.getLocationID());
        glBindTexture(GL_TEXTURE_CUBE_MAP, environment.prefilterID());
        glActiveTexture(TextureUnit.Brdf.getLocationID());
        glBindTexture(GL_TEXTURE_2D, environment.brdfID());
        glActiveTexture(GL_TEXTURE0);
    }

    /** Releases the GL objects, the next frame with a skybox creates them again */
    public void release() {
        dropEnvironment();

        if (brdfID != 0) {
            generator.releaseBrdf(brdfID);
            brdfID = 0;
        }
    }

    private void build(final CubemapEntityImplementation skybox) {
        if (generator == null)
            generator = new EnvironmentHelper(memory, name -> programRegistry.get(name).getInternalID());

        final var started = System.nanoTime();
        if (brdfID == 0)
            brdfID = generator.generateBrdf();

        environment = generator.generate(skybox.getInternalID(), brdfID);
        this.skybox = skybox;

        log.info("Environment of the skybox {} was convolved in {} ms", skybox.getID(), (System.nanoTime() - started) / 1_000_000);
    }

    private void dropEnvironment() {
        if (environment != null) {
            generator.release(environment);
            environment = null;
        }

        skybox = null;
    }
}
