package io.github.trimax.venta.engine.context;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.EntityType;
import io.github.trimax.venta.engine.managers.AbstractManager;
import io.github.trimax.venta.engine.model.view.AbstractView;
import io.github.trimax.venta.engine.utils.TransformationUtil;

import java.util.List;
import java.util.Map;

@Component
public final class ManagerContext {
    private final Map<EntityType, AbstractManager<?, ?>> managers;

    private ManagerContext(final List<AbstractManager<?, ?>> managers) {
        this.managers = TransformationUtil.toMap(managers, AbstractManager::getEntityType);
    }

    @SuppressWarnings("unchecked")
    public <E extends V, V extends AbstractView, M extends AbstractManager<E, V>> M get(final EntityType type) {
        return (M) managers.get(type);
    }
}
