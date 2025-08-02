package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.EntityType;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.model.dto.LightDTO;
import io.github.trimax.venta.engine.model.view.LightView;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LightManagerImplementation extends AbstractManagerImplementation<LightManagerImplementation.LightEntity, LightView> {
    private final GizmoManagerImplementation.GizmoAccessor gizmoAccessor;
    private final ResourceManagerImplementation resourceManager;
    private final GizmoManagerImplementation gizmoManager;

    public LightView load(final String name) {
        log.info("Loading light {}", name);

        return store(new LightEntity(name, resourceManager.load(String.format("/lights/%s.json", name), LightDTO.class),
                gizmoAccessor.get(gizmoManager.create("light", GizmoType.Light))));
    }

    @Override
    protected void destroy(final LightEntity light) {
        log.info("Destroying light {} ({})", light.getID(), light.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.Light;
    }

    @Getter
    public static final class LightEntity extends AbstractEntity implements LightView {
        private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);
        private final Vector3f direction = new Vector3f(0.f, 0.f, 0.f);
        private final Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);

        private Attenuation attenuation = new Attenuation(1.0f, 0.1f, 0.01f);
        private float intensity = 1.f;

        LightEntity(@NonNull final String name, @NonNull final GizmoManagerImplementation.GizmoEntity gizmo) {
            super(gizmo, name);
        }

        LightEntity(@NonNull final String name, @NonNull final LightDTO dto, @NonNull final GizmoManagerImplementation.GizmoEntity gizmo) {
            this(name, gizmo);

            setPosition(dto.position());
            setDirection(dto.direction());
            setColor(dto.color());
            setAttenuation(attenuation);
        }

        @Override
        public void setPosition(final Vector3f position) {
            this.position.set(position);
        }

        @Override
        public void setDirection(final Vector3f direction) {
            this.direction.set(direction);
        }

        @Override
        public void setColor(final Vector3f color) {
            this.color.set(color);
        }

        @Override
        public void setAttenuation(final Attenuation attenuation) {
            this.attenuation = attenuation;
        }

        @Override
        public void setIntensity(final float intensity) {
            this.intensity = intensity;
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class LightAccessor extends AbstractAccessor {}
}
