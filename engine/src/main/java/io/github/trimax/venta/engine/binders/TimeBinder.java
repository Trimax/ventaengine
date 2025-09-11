package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final Engine.VentaTime time) {
        bind(program.getUniformID(ShaderUniform.TimeElapsed), (float) time.getTimeElapsed());
        bind(program.getUniformID(ShaderUniform.TimeDelta), (float) time.getDelta());
    }
}
