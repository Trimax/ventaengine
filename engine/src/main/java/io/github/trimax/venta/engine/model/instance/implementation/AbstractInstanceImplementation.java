package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import io.github.trimax.venta.engine.utils.IdentifierUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractInstanceImplementation implements AbstractInstance {
    private final String id = IdentifierUtil.generate(6);
    private final GizmoInstanceImplementation gizmo;
    private final String name;

    /** The scene the instance has been added to, or null; an instance belongs to one scene at most */
    private SceneInstanceImplementation parent;

    protected AbstractInstanceImplementation(final String name) {
        this(null, name);
    }

    public final SceneInstanceImplementation getParent() {
        return parent;
    }

    /**
     * Only scenes change it, keeping it consistent with their contents.
     * An instance belongs to one scene at most: its state (e.g. position) would otherwise change in all of them.
     * Null detaches the instance from its scene.
     */
    final void setParent(final SceneInstanceImplementation parent) {
        if (parent != null && this.parent != null && this.parent != parent)
            throw new IllegalStateException(String.format(
                    "%s is already in scene %s. Remove it from that scene first or create another instance from the same prefab",
                    this, this.parent.getName()));

        this.parent = parent;
    }

    /** Detaches the instance from its scene */
    final void removeParent() {
        setParent(null);
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
