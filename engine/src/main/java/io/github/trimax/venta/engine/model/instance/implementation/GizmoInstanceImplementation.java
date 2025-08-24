package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.GizmoInstance;
import lombok.Getter;
import org.joml.Vector3f;

@Getter
public final class GizmoInstanceImplementation extends AbstractInstanceImplementation implements GizmoInstance {
    private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f rotation = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f scale = new Vector3f(1.f, 1.f, 1.f);
    private final ProgramEntityImplementation program;
    private final MeshEntityImplementation mesh;

    GizmoInstanceImplementation(final String name,
                                       final ProgramEntityImplementation program,
                                       final MeshEntityImplementation mesh,
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

    public void setScale(final Vector3f scale) {
        this.scale.set(scale);
    }
}
