package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.view.MaterialView;
import lombok.NonNull;

public interface MaterialManager extends AbstractManager<MaterialView> {
    MaterialView load(@NonNull final String name);
}
