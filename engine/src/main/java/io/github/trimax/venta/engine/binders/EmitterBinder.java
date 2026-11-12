package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.EmitterInstance;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmitterBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final EmitterInstance emitter) {
        if (emitter == null)
            return;

        bind(program.getUniformID(ShaderUniform.UseLighting), emitter.isLit() ? 1 : 0);
    }
}
