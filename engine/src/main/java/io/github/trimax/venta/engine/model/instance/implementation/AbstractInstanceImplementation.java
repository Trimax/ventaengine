package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import io.github.trimax.venta.engine.utils.IdentifierUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractInstanceImplementation implements AbstractInstance {
    private final String id = IdentifierUtil.generate(6);
    private final GizmoInstanceImplementation gizmo;
    private final String name;

    protected AbstractInstanceImplementation(final String name) {
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

    public final GizmoInstanceImplementation getGizmo() {
        return gizmo;
    }

    @Override
    public final String toString() {
        return String.format("%s-%s-%s", getClass().getSimpleName(), id, name);
    }
}
