package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.common.math.Fresnel;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FresnelBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final Fresnel fresnel) {

        bind(program.getUniformID(ShaderUniform.FresnelScale), fresnel.scale());
        bind(program.getUniformID(ShaderUniform.FresnelPower), fresnel.power());
    }
}