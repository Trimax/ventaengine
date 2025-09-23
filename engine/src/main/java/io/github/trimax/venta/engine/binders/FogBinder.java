package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.common.effects.Fog;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FogBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final Fog fog) {
        bind(program.getUniformID(ShaderUniform.UseFogFlag), fog != null);

        if (fog == null)
            return;

        bind(program.getUniformID(ShaderUniform.FogMinimalDistance), fog.getMinimalDistance());
        bind(program.getUniformID(ShaderUniform.FogMaximalDistance), fog.getMaximalDistance());
        bind(program.getUniformID(ShaderUniform.FogColor), fog.getColor());
    }
}
