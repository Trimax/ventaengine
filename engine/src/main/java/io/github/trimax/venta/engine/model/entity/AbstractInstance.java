package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.model.view.AbstractView;
import io.github.trimax.venta.engine.utils.IdentifierUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractInstance implements AbstractView {
    private final String id = IdentifierUtil.generate(6);
    private final GizmoInstance gizmo;
    private final String name;

    protected AbstractInstance(final String name) {
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

    public final GizmoInstance getGizmo() {
        return gizmo;
    }
}
