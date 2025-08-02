package io.github.trimax.venta.engine.context;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.EntityType;
import io.github.trimax.venta.engine.managers.implementation.AbstractManagerImplementation;
import io.github.trimax.venta.engine.model.view.AbstractView;
import io.github.trimax.venta.engine.utils.TransformationUtil;

import java.util.List;
import java.util.Map;

@Component
public final class ManagerContext {
    private final Map<EntityType, AbstractManagerImplementation<?, ?>> managers;

    private ManagerContext(final List<AbstractManagerImplementation<?, ?>> managers) {
        this.managers = TransformationUtil.toMap(managers, AbstractManagerImplementation::getEntityType);
    }

    @SuppressWarnings("unchecked")
    public <E extends V, V extends AbstractView, M extends AbstractManagerImplementation<E, V>> M get(final EntityType type) {
        return (M) managers.get(type);
    }
}
