package com.venta.engine.managers;

import com.venta.engine.annotations.Component;
import com.venta.engine.model.core.Couple;
import com.venta.engine.model.dto.LightDTO;
import com.venta.engine.model.view.LightView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class LightManager extends AbstractManager<LightManager.LightEntity, LightView> {
    private final ResourceManager resourceManager;

    public LightView load(final String name) {
        log.info("Loading light {}", name);

        final var lightDTO = resourceManager.load("/lights/" + name, LightDTO.class);

        final var light = store(new LightEntity(lightDTO));
        light.setPosition(light.getPosition());
        light.setDirection(light.getDirection());
        light.setColor(light.getColor());

        return light;
    }

    @Override
    protected LightView createView(final String id, final LightEntity entity) {
        return new LightView(id, entity);
    }

    @Override
    protected void destroy(final Couple<LightEntity, LightView> object) {
        log.info("Unloading light {}", object.entity().name);
    }

    @Getter
    public static final class LightEntity extends AbstractEntity {
        private final String name;

        LightEntity(@NonNull final String name) {
            super(0L);

            this.name = name;
        }

        LightEntity(@NonNull final LightDTO dto) {
            this(dto.name());
        }
    }
}
