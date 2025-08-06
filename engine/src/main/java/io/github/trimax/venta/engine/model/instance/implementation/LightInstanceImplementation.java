package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.dto.LightDTO;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.joml.Vector3f;

@Getter
public final class LightInstanceImplementation extends AbstractInstanceImplementation implements LightInstance {
    private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f direction = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);

    private final LightType type;
    private Attenuation attenuation = new Attenuation(1.0f, 0.1f, 0.01f);
    private float intensity = 1.f;

    @Setter
    private boolean castShadows = false;

    @Setter
    private boolean enabled = true;

    LightInstanceImplementation(@NonNull final String name,
                                @NonNull final LightType type,
                                @NonNull final GizmoInstanceImplementation gizmo) {
        super(gizmo, name);

        this.type = type;
    }

    public LightInstanceImplementation(@NonNull final String name, @NonNull final LightDTO dto, @NonNull final GizmoInstanceImplementation gizmo) {
        this(name, dto.type(), gizmo);

        setColor(dto.color());
        setAttenuation(dto.attenuation().getAttenuation());
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
