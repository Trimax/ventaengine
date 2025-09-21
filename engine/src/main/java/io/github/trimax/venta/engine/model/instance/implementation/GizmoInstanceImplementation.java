package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.instance.GizmoInstance;
import lombok.Getter;
import lombok.NonNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Getter
public final class GizmoInstanceImplementation extends AbstractInstanceImplementation implements GizmoInstance {
    private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f rotation = new Vector3f(0.f, 0.f, 0.f);
    private final Vector3f scale = new Vector3f(1.f, 1.f, 1.f);
    private final ProgramEntityImplementation program;
    private final MeshEntityImplementation mesh;

    GizmoInstanceImplementation(@NonNull final String name,
                                @NonNull final ProgramEntityImplementation program,
                                @NonNull final MeshEntityImplementation mesh,
                                @NonNull final Vector3fc position,
                                @NonNull final Vector3fc rotation,
                                @NonNull final Vector3fc scale) {
        super(name);

        this.mesh = mesh;
        this.program = program;

        this.position.set(position);
        this.rotation.set(rotation);
        this.scale.set(scale);
    }

    public void setScale(@NonNull final Vector3fc scale) {
        this.scale.set(scale);
    }
}
