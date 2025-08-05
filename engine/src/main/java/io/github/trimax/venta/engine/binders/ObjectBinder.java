package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectBinder extends AbstractBinder {
    public void bind(final ProgramEntityImplementation program, final ObjectInstance object) {
        if (object == null)
            return;

        bind(program.getUniformID(ShaderUniform.UseLighting), object.isLit() ? 1 : 0);
    }
}
