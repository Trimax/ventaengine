package io.github.trimax.venta.engine.context;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.*;
import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import io.github.trimax.venta.engine.registries.implementation.*;
import io.github.trimax.venta.engine.utils.TransformationUtil;
import lombok.NonNull;
import one.util.streamex.StreamEx;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public final class ManagerContext {
    private final Map<Class<?>, AbstractRegistryImplementation<?, ?, ?>> registries;
    private final Map<Class<?>, AbstractManagerImplementation<?, ?>> managers;

    private ManagerContext(final List<AbstractRegistryImplementation<?, ?, ?>> registries,
                           final List<AbstractManagerImplementation<?, ?>> managers) {
        this.registries = TransformationUtil.toMap(registries, AbstractRegistryImplementation::getClass);
        this.managers = TransformationUtil.toMap(managers, AbstractManagerImplementation::getClass);
    }

    public <E extends V, V extends AbstractInstance, M extends AbstractManagerImplementation<E, V>> M get(@NonNull final Class<M> managerClass) {
        return managerClass.cast(managers.get(managerClass));
    }

    public void cleanup() {
        StreamEx.of(MANAGER_CLEANUP_ORDER).map(managers::get).forEach(AbstractManagerImplementation::cleanup);
        StreamEx.of(REGISTRY_CLEANUP_ORDER).map(registries::get).forEach(AbstractRegistryImplementation::cleanup);
    }

    /* The cleanup order of managers */
    private static final List<Class<? extends AbstractManagerImplementation<?, ?>>> MANAGER_CLEANUP_ORDER = Arrays.asList(
            SceneManagerImplementation.class,
            LightManagerImplementation.class,
            ObjectManagerImplementation.class,
            GizmoManagerImplementation.class,
            MeshManagerImplementation.class,
            CameraManagerImplementation.class,
            MaterialManagerImplementation.class,
            AtlasManagerImplementation.class);

    /* The cleanup order of registries */
    private static final List<Class<? extends AbstractRegistryImplementation<?, ?, ?>>> REGISTRY_CLEANUP_ORDER = Arrays.asList(
            FontRegistryImplementation.class,
            AtlasRegistryImplementation.class,
            ProgramRegistryImplementation.class,
            ShaderRegistryImplementation.class,
            TextureRegistryImplementation.class);
}
