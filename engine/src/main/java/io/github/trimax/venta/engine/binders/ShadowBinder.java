package io.github.trimax.venta.engine.binders;

import org.lwjgl.system.MemoryStack;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.TextureUnit;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.renderers.common.ShadowRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Gives the shaders the shadows of the sun of the frame: the cascades and the unit with the shadow map.
 */
@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowBinder extends AbstractBinder {
    private final ShadowRenderer shadowRenderer;

    public void bind(final ProgramEntityImplementation program) {
        final var frame = shadowRenderer.getFrame();

        // The sampler of shadows must never share a unit with a sampler of another type, so it is set even without shadows
        bind(program.getUniformID(ShaderUniform.TextureShadow), TextureUnit.Shadow.getId());
        bind(program.getUniformID(ShaderUniform.UseShadows), frame != null);

        if (frame == null)
            return;

        final var cascades = frame.cascades();
        final var count = Math.min(cascades.size(), DefinitionsCommon.MAX_CASCADES);

        bind(program.getUniformID(ShaderUniform.ShadowCascadeCount), count);
        bind(program.getUniformID(ShaderUniform.ShadowDistance), frame.distance());
        bind(program.getUniformID(ShaderUniform.CameraForward), frame.cameraForward());

        try (final var stack = MemoryStack.stackPush()) {
            final var matrices = stack.mallocFloat(16 * count);
            final var splits = new float[count];
            final var texels = new float[count];

            for (int i = 0; i < count; i++) {
                cascades.get(i).matrix().get(16 * i, matrices);
                splits[i] = cascades.get(i).farDistance();
                texels[i] = cascades.get(i).texelSize();
            }

            bindMatrix4(program.getUniformID(ShaderUniform.ShadowMatrices), matrices);
            bind(program.getUniformID(ShaderUniform.ShadowSplits), splits);
            bind(program.getUniformID(ShaderUniform.ShadowTexels), texels);
        }
    }
}
