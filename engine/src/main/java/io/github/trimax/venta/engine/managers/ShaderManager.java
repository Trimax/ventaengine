package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.entities.ShaderEntity;
import io.github.trimax.venta.engine.model.view.ShaderView;
import lombok.NonNull;

public interface ShaderManager extends AbstractManager<ShaderView> {
    ShaderView load(@NonNull final String name,
                    @NonNull final ShaderEntity.Type type);
}
