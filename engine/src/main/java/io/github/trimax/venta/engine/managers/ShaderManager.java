package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.managers.implementation.ShaderManagerImplementation;
import io.github.trimax.venta.engine.model.view.ShaderView;
import lombok.NonNull;

public interface ShaderManager extends AbstractManager<ShaderView> {
    ShaderView load(@NonNull final String name,
                    @NonNull final ShaderManagerImplementation.ShaderEntity.Type type);
}
