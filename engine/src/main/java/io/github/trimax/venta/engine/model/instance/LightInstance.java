package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.model.dto.LightDTO;
import io.github.trimax.venta.engine.model.view.LightView;
import lombok.Getter;
import lombok.NonNull;
import org.joml.Vector3f;

@Getter
public final class LightInstance extends AbstractInstance implements LightView {
    private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f direction = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);

    private Attenuation attenuation = new Attenuation(1.0f, 0.1f, 0.01f);
    private float intensity = 1.f;

    LightInstance(@NonNull final String name, @NonNull final GizmoInstance gizmo) {
        super(gizmo, name);
    }

    public LightInstance(@NonNull final String name, @NonNull final LightDTO dto, @NonNull final GizmoInstance gizmo) {
        this(name, gizmo);

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
