package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.common.water.WaterFoam;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WaterFoamBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final WaterFoam foam) {
        bind(program.getUniformID(ShaderUniform.UseFoam), foam != null);

        if (foam == null)
            return;

        bind(program.getUniformID(ShaderUniform.FoamThreshold), foam.getThreshold());
        bind(program.getUniformID(ShaderUniform.FoamIntensity), foam.getIntensity());
    }
}
