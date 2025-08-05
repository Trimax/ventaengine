package io.github.trimax.venta.engine.context;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.AbstractManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.AtlasManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.CameraManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ConsoleItemManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ConsoleManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.FontManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.GizmoManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.LightManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.MaterialManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.MeshManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ObjectManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ProgramManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ResourceManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.SceneManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ShaderManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.TextureManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.WindowManagerImplementation;
import io.github.trimax.venta.engine.model.view.AbstractView;
import io.github.trimax.venta.engine.utils.TransformationUtil;
import lombok.NonNull;
import one.util.streamex.StreamEx;

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
            WindowManagerImplementation.class,
            MaterialManagerImplementation.class,
            FontManagerImplementation.class,
            AtlasManagerImplementation.class,
            ConsoleManagerImplementation.class,
            ConsoleItemManagerImplementation.class,
            TextureManagerImplementation.class,
            ResourceManagerImplementation.class);
}
