package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.MaterialInstance;
import lombok.NonNull;

public interface MaterialManager extends AbstractManager<MaterialInstance> {
    MaterialInstance load(@NonNull final String name);
}
