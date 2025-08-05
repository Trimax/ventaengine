package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.ProgramInstance;
import lombok.NonNull;

public interface ProgramManager extends AbstractManager<ProgramInstance> {
    ProgramInstance load(@NonNull final String name);
}
