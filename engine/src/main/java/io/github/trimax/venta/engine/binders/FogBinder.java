package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.common.scene.Fog;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FogBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final Fog fog) {
        if (fog == null) {
            bind(program.getUniformID(ShaderUniform.FogEnabled), false);
            return;
        }

        bind(program.getUniformID(ShaderUniform.FogType), fog.type().getValue());
        bind(program.getUniformID(ShaderUniform.FogEnabled), true);
        bind(program.getUniformID(ShaderUniform.FogColor), fog.color());
        bind(program.getUniformID(ShaderUniform.FogDensity), fog.density());
        bind(program.getUniformID(ShaderUniform.FogStart), fog.start());
        bind(program.getUniformID(ShaderUniform.FogEnd), fog.end());
    }
}
