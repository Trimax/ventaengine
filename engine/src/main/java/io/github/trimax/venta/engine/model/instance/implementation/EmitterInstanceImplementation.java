package io.github.trimax.venta.engine.model.instance.implementation;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.joml.Vector3f;
import org.joml.Vector3fc;

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

@Getter
public final class EmitterInstanceImplementation extends AbstractInstanceImplementation implements EmitterInstance {
    private final List<Particle> particles = new ArrayList<>();
    private final Transform transform = new Transform();
    private final Vector3f velocity = new Vector3f();
    private final Vector3f deviation = new Vector3f();
    private final ProgramEntityImplementation program;

    private final float maximalSize;
    private final float minimalSize;
    private final float minimalLifetime;
    private final float lifetimeDeviation;
    private final int maximalParticlesCount;

    private final FloatBuffer bufferMatrixModel;
    private final FloatBuffer bufferColor;

    private final int particleVertexArrayObjectID;
    private final int particleVerticesBufferID;
    private final int particleInstanceBufferID;
    private final int particleFacesBufferID;
    private final int particleColorBufferID;

    @Setter
    private float emissionRate;

    private float emissionAccumulator = 0f;

    private TextureEntityImplementation texture;

    EmitterInstanceImplementation(@NonNull final String name,
                                  @NonNull final EmitterPrefabImplementation prefab,
                                  @NonNull final TextureEntityImplementation texture,
                                  @NonNull final ProgramEntityImplementation program,
                                  @NonNull final GizmoInstanceImplementation gizmo,
                                  @NonNull final FloatBuffer bufferMatrixModel,
                                  @NonNull final FloatBuffer bufferColor,
                                  final int particleVertexArrayObjectID,
                                  final int particleVerticesBufferID,
                                  final int particleInstanceBufferID,
                                  final int particleFacesBufferID,
                                  final int particleColorBufferID) {
        super(gizmo, name);

        this.program = program;
        this.texture = texture;
        this.bufferColor = bufferColor;
        this.emissionRate = Math.max(0.f, prefab.getDto().emissionRate());
        this.maximalSize = (float) Math.max(0.0, prefab.getDto().maximalSize());
        this.minimalSize = (float) Math.max(0.0, prefab.getDto().minimalSize());
        this.minimalLifetime = (float) Math.max(0.0, prefab.getDto().minimalLifetime());
        this.lifetimeDeviation = (float) Math.max(0.0, prefab.getDto().lifetimeDeviation());
        this.maximalParticlesCount = Math.max(1, prefab.getDto().particlesCount());
        this.bufferMatrixModel = bufferMatrixModel;
        this.particleColorBufferID = particleColorBufferID;
        this.particleFacesBufferID = particleFacesBufferID;
        this.particleVerticesBufferID = particleVerticesBufferID;
        this.particleInstanceBufferID = particleInstanceBufferID;
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
