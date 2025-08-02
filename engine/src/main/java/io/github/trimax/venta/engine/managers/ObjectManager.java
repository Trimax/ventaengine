package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.view.MeshView;
import io.github.trimax.venta.engine.model.view.ObjectView;
import io.github.trimax.venta.engine.model.view.ProgramView;
import lombok.NonNull;

public interface ObjectManager extends AbstractManager<ObjectView> {
    ObjectView load(@NonNull final String name);

    ObjectView create(@NonNull final String name,
                      @NonNull final MeshView mesh,
                      @NonNull final ProgramView program);
}
