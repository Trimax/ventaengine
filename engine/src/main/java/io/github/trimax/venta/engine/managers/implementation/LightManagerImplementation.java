package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.managers.LightManager;
import io.github.trimax.venta.engine.model.dto.LightDTO;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.view.LightView;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LightManagerImplementation
        extends AbstractManagerImplementation<LightInstance, LightView>
        implements LightManager {
    private final GizmoManagerImplementation gizmoManager;

    @Override
    public LightInstance load(@NonNull final String name) {
        log.info("Loading light {}", name);

        return store(new LightInstance(name, ResourceUtil.loadAsObject(String.format("/lights/%s.json", name), LightDTO.class),
                gizmoManager.create("light", GizmoType.Light)));
    }

    @Override
    protected void destroy(final LightInstance light) {
        log.info("Destroying light {} ({})", light.getID(), light.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }
}
