package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.model.view.GizmoView;
import lombok.Getter;
import org.joml.Vector3f;

@Getter
public final class GizmoEntity extends AbstractEntity implements GizmoView {
    private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f rotation = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f scale = new Vector3f(1.f, 1.f, 1.f);
    private final ProgramEntity program;
    private final MeshEntity mesh;

    public GizmoEntity(final String name,
                       final ProgramEntity program,
                       final MeshEntity mesh,
                       final Vector3f position,
                       final Vector3f rotation,
                       final Vector3f scale) {
        super(name);

        this.mesh = mesh;
        this.program = program;

        this.position.set(position);
        this.rotation.set(rotation);
        this.scale.set(scale);
    }
}
