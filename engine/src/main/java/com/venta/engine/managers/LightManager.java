package com.venta.engine.managers;

import org.joml.Vector3f;

import com.venta.engine.annotations.Component;
import com.venta.engine.enums.GizmoType;
import com.venta.engine.model.dto.LightDTO;
import com.venta.engine.model.view.LightView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LightManager extends AbstractManager<LightManager.LightEntity, LightView> {
    private final GizmoManager.GizmoAccessor gizmoAccessor;
    private final ResourceManager resourceManager;
    private final GizmoManager gizmoManager;

    public LightView load(final String name) {
        log.info("Loading light {}", name);

        return store(new LightEntity(resourceManager.load(String.format("/lights/%s.json", name), LightDTO.class),
                gizmoAccessor.get(gizmoManager.create("light", GizmoType.Light))));
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }

    @Override
    protected void destroy(final LightEntity light) {
        log.info("Destroying light {} ({})", light.getID(), light.getName());
    }

    @Getter
    public static final class LightEntity extends AbstractEntity implements LightView {
        private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);
        private final Vector3f direction = new Vector3f(0.f, 0.f, 0.f);
        private final Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);
        private final GizmoManager.GizmoEntity gizmo;

        private Attenuation attenuation = new Attenuation(1.0f, 0.1f, 0.01f);
        private float intensity = 1.f;

        LightEntity(@NonNull final String name, @NonNull final GizmoManager.GizmoEntity gizmo) {
            super(name);

            this.gizmo = gizmo;
        }

        LightEntity(@NonNull final LightDTO dto, @NonNull final GizmoManager.GizmoEntity gizmo) {
            this(dto.name(), gizmo);

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
