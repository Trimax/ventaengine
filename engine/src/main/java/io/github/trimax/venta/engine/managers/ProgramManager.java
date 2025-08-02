package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.view.ProgramView;
import lombok.NonNull;

public interface ProgramManager extends AbstractManager<ProgramView> {
    ProgramView load(@NonNull final String name);
}
