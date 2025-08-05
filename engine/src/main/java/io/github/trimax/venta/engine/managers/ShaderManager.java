package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.ShaderInstance;
import io.github.trimax.venta.engine.model.instance.implementation.ShaderInstanceImplementation;
import lombok.NonNull;

public interface ShaderManager extends AbstractManager<ShaderInstance> {
    ShaderInstance load(@NonNull final String name,
                        @NonNull final ShaderInstanceImplementation.Type type);
}
