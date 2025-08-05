package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.model.instance.GizmoInstance;
import lombok.Getter;
import org.joml.Vector3f;

@Getter
public final class GizmoInstanceImplementation extends AbstractInstanceImplementation implements GizmoInstance {
    private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f rotation = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f scale = new Vector3f(1.f, 1.f, 1.f);
    private final ProgramInstanceImplementation program;
    private final MeshInstanceImplementation mesh;

    public GizmoInstanceImplementation(final String name,
                                       final ProgramInstanceImplementation program,
                                       final MeshInstanceImplementation mesh,
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
