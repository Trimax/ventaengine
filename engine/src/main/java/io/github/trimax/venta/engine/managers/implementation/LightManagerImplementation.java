package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.exceptions.UnknownInstanceException;
import io.github.trimax.venta.engine.managers.LightManager;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.instance.implementation.LightInstanceImplementation;
import io.github.trimax.venta.engine.model.parameters.LightParameters;
import io.github.trimax.venta.engine.model.prefabs.LightPrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.LightPrefabImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LightManagerImplementation
        extends AbstractManagerImplementation<LightInstanceImplementation, LightInstance>
        implements LightManager {
    private final GizmoManagerImplementation gizmoManager;

    @Override
    public LightInstanceImplementation create(@NonNull final String name, @NonNull final LightPrefab prefab) {
        if (prefab instanceof LightPrefabImplementation light)
            return create(name, convert(light));

        throw new UnknownInstanceException(prefab.getClass());
    }

    @Override
    public LightInstanceImplementation create(@NonNull final String name, @NonNull final LightParameters parameters) {
        log.info("Loading light {}", name);

        return store(new LightInstanceImplementation(name, parameters, gizmoManager.create("light", GizmoType.Light)));
    }

    private LightParameters convert(final LightPrefabImplementation prefab) {
        return LightParameters.builder()
                .type(prefab.getType())
                .range(prefab.getRange())
                .color(prefab.getColor())
                .intensity(prefab.getIntensity())
                .attenuation(prefab.getAttenuation())
                .castShadows(prefab.isCastShadows())
                .build();
    }

    @Override
    public void delete(@NonNull final LightInstance instance) {
        if (instance instanceof LightInstanceImplementation light)
            super.delete(light);
    }

    @Override
    protected void destroy(final LightInstanceImplementation light) {
        log.info("Destroying light {} ({})", light.getID(), light.getName());
    }
}
