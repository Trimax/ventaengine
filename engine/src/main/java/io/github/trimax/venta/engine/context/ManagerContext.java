package io.github.trimax.venta.engine.context;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.*;
import io.github.trimax.venta.engine.model.view.AbstractView;
import io.github.trimax.venta.engine.utils.TransformationUtil;
import lombok.NonNull;
import one.util.streamex.StreamEx;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public final class ManagerContext {
    private final Map<Class<?>, AbstractManagerImplementation<?, ?>> managers;

    private ManagerContext(final List<AbstractManagerImplementation<?, ?>> managers) {
        this.managers = TransformationUtil.toMap(managers, AbstractManagerImplementation::getClass);
    }

    public <E extends V, V extends AbstractView, M extends AbstractManagerImplementation<E, V>> M get(@NonNull final Class<M> managerClass) {
        return managerClass.cast(managers.get(managerClass));
    }

    public void cleanup() {
        StreamEx.of(CLEANUP_ORDER).map(managers::get).forEach(AbstractManagerImplementation::cleanup);
    }

    /* The cleanup order is important */
    private static final List<Class<? extends AbstractManagerImplementation<?, ?>>> CLEANUP_ORDER = Arrays.asList(
            SceneManagerImplementation.class,
            LightManagerImplementation.class,
            ObjectManagerImplementation.class,
            GizmoManagerImplementation.class,
            MeshManagerImplementation.class,
            ProgramManagerImplementation.class,
            ShaderManagerImplementation.class,
            CameraManagerImplementation.class,
            MaterialManagerImplementation.class,
            FontManagerImplementation.class,
            AtlasManagerImplementation.class,
            ConsoleItemManagerImplementation.class,
            TextureManagerImplementation.class);
}
