package com.venta.engine.model.view;

import org.joml.Vector3f;
import org.joml.Vector4f;

import com.venta.engine.managers.LightManager;
import com.venta.engine.renderers.AbstractRenderer;
import lombok.Getter;

public final class LightView extends AbstractRenderer.AbstractView<LightManager.LightEntity> {
    @Getter
    private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);

    @Getter
    private final Vector3f direction = new Vector3f(0.f, 0.f, 0.f);

    @Getter
    private final Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    @Getter
    private final Attenuation attenuation = new Attenuation(1.0f, 0.1f, 0.01f);

    public LightView(final LightManager.LightEntity entity) {
        super(entity);
    }

    public void setPosition(final Vector3f position) {
        this.position.set(position);
    }

    public void setDirection(final Vector3f position) {
        this.direction.set(position);
    }

    public void setColor(final Vector4f color) {
        this.color.set(color);
    }

    public record Attenuation(float constant, float linear, float quadratic) {}
}
