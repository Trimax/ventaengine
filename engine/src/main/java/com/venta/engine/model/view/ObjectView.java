package com.venta.engine.model.view;

import com.venta.engine.managers.ObjectManager;
import com.venta.engine.renderers.AbstractRenderer;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

@Getter
public final class ObjectView extends AbstractRenderer.AbstractView<ObjectManager.ObjectEntity> {
    private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f rotation = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f scale = new Vector3f(1.f, 1.f, 1.f);

    @Setter
    private ProgramView program;

    public ObjectView(final ObjectManager.ObjectEntity entity) {
        super(entity);
    }

    public void setPosition(final Vector3f position) {
        this.position.set(position);
    }

    public void move(final Vector3f offset) {
        this.position.add(offset, this.position);
    }

    public void setRotation(final Vector3f rotation) {
        this.rotation.set(rotation);
    }

    public void rotate(final Vector3f angles) {
        this.rotation.add(angles, this.rotation);
    }

    public void setScale(final Vector3f scale) {
        this.scale.set(scale);
    }

    public void scale(final Vector3f factor) {
        this.scale.add(factor, this.scale);
    }
}
