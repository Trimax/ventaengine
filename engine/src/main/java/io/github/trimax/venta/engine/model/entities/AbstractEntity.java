package io.github.trimax.venta.engine.model.entities;

import io.github.trimax.venta.engine.model.view.AbstractView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractEntity implements AbstractView {
    private final String id = UUID.randomUUID().toString();
    private final GizmoEntity gizmo;
    private final String name;

    protected AbstractEntity(final String name) {
        this(null, name);
    }

    @Override
    public final String getID() {
        return id;
    }

    @Override
    public final String getName() {
        return name;
    }

    public final GizmoEntity getGizmo() {
        return gizmo;
    }
}
