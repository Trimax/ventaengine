package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.model.common.effects.Particle;
import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.entity.TextureEntity;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import io.github.trimax.venta.engine.model.instance.EmitterInstance;
import io.github.trimax.venta.engine.model.prefabs.implementation.EmitterPrefabImplementation;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public final class EmitterInstanceImplementation extends AbstractInstanceImplementation implements EmitterInstance {
    private final List<Particle> particles = new ArrayList<>();
    private final Transform transform = new Transform();
    private final Vector3f velocity = new Vector3f();
    private final Vector3f deviation = new Vector3f();
    private final ProgramEntityImplementation program;
    private final int particleVertexArrayObjectID;
    private final int particleVerticesBufferID;

    @Setter
    private float emissionRate;

    private float emissionAccumulator = 0f;

    private TextureEntityImplementation texture;

    EmitterInstanceImplementation(@NonNull final String name,
                                  @NonNull final EmitterPrefabImplementation prefab,
                                  @NonNull final TextureEntityImplementation texture,
                                  @NonNull final ProgramEntityImplementation program,
                                  @NonNull final GizmoInstanceImplementation gizmo,
                                  final int particleVertexArrayObjectID,
                                  final int particleVerticesBufferID) {
        super(gizmo, name);

        this.program = program;
        this.texture = texture;
        this.emissionRate = prefab.getDto().emissionRate();
        this.particleVerticesBufferID = particleVerticesBufferID;
        this.particleVertexArrayObjectID = particleVertexArrayObjectID;
        Optional.ofNullable(prefab.getDto().deviation()).ifPresent(deviation::set);
        Optional.ofNullable(prefab.getDto().velocity()).ifPresent(velocity::set);
    }

    public void updateEmissionAccumulator(final float delta) {
        emissionAccumulator += delta;
    }

    @Override
    public Vector3fc getPosition() {
        return transform.getPosition();
    }

    @Override
    public void setPosition(@NonNull final Vector3fc position) {
        this.transform.setPosition(position);
    }

    @Override
    public void setVelocity(@NonNull final Vector3fc velocity) {
        this.velocity.set(velocity);
    }

    @Override
    public void setDeviation(@NonNull final Vector3fc deviation) {
        this.deviation.set(deviation);
    }

    @Override
    public void setTexture(@NonNull final TextureEntity texture) {
        if (texture instanceof TextureEntityImplementation entity)
            this.texture = entity;
    }
}
