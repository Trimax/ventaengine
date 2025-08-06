package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.entity.MeshPrefab;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import lombok.NonNull;

public interface ObjectManager extends AbstractManager<ObjectInstance> {
    ObjectInstance load(@NonNull final String name);

    ObjectInstance create(@NonNull final String name,
                          @NonNull final MeshPrefab mesh,
                          @NonNull final ProgramEntity program);
}
