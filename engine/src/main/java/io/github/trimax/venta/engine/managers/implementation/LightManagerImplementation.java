package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.managers.LightManager;
import io.github.trimax.venta.engine.model.dto.LightDTO;
import io.github.trimax.venta.engine.model.entities.LightEntity;
import io.github.trimax.venta.engine.model.view.LightView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LightManagerImplementation
        extends AbstractManagerImplementation<LightEntity, LightView>
        implements LightManager {
    private final ResourceManagerImplementation resourceManager;
    private final GizmoManagerImplementation gizmoManager;

    @Override
    public LightEntity load(@NonNull final String name) {
        log.info("Loading light {}", name);

        return store(new LightEntity(name, resourceManager.load(String.format("/lights/%s.json", name), LightDTO.class),
                gizmoManager.create("light", GizmoType.Light)));
    }

    @Override
    protected void destroy(final LightEntity light) {
        log.info("Destroying light {} ({})", light.getID(), light.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }
}
