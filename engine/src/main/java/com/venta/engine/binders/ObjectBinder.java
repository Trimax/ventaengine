package com.venta.engine.binders;

import com.venta.engine.annotations.Component;
import com.venta.engine.enums.ShaderUniform;
import com.venta.engine.managers.ProgramManager;
import com.venta.engine.model.view.ObjectView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectBinder extends AbstractBinder {
    public void bind(final ProgramManager.ProgramEntity program, final ObjectView object) {
        if (object == null)
            return;

        bind(program.getUniformID(ShaderUniform.UseLighting), object.isLit() ? 1 : 0);
    }
}
