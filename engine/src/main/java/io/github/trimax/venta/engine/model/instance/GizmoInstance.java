package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.model.view.GizmoView;
import lombok.Getter;
import org.joml.Vector3f;

@Getter
public final class GizmoInstance extends AbstractInstance implements GizmoView {
    private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f rotation = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f scale = new Vector3f(1.f, 1.f, 1.f);
    private final ProgramInstance program;
    private final MeshInstance mesh;

    public GizmoInstance(final String name,
                         final ProgramInstance program,
                         final MeshInstance mesh,
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
