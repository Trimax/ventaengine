package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.model.common.dto.Range;
import io.github.trimax.venta.engine.model.common.dto.Variable;
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

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public final class EmitterInstanceImplementation extends AbstractInstanceImplementation implements EmitterInstance {
    private final List<Particle> particles = new ArrayList<>();
    private final Transform transform = new Transform();
    private final ProgramEntityImplementation program;

    private final Vector3f particleVelocity = new Vector3f();
    private final Vector3f particleVelocityDeviation = new Vector3f();
    private final Range<Float> particleSize;
    private final Variable<Float> particleLifetime;
    private final Variable<Float> particleAngularVelocity;

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
        this.emissionRate = prefab.getEmissionRate();
        this.particleSize = prefab.getParticleSize();
        this.particleLifetime = prefab.getParticleLifetime();
        this.particleAngularVelocity = prefab.getParticleAngularVelocity();
        this.maximalParticlesCount = prefab.getParticlesCount();
        this.bufferMatrixModel = bufferMatrixModel;
        this.particleColorBufferID = particleColorBufferID;
        this.particleFacesBufferID = particleFacesBufferID;
        this.particleVerticesBufferID = particleVerticesBufferID;
        this.particleInstanceBufferID = particleInstanceBufferID;
        this.particleVertexArrayObjectID = particleVertexArrayObjectID;
        Optional.of(prefab.getParticleVelocity()).map(Variable::deviation).ifPresent(particleVelocityDeviation::set);
        Optional.of(prefab.getParticleVelocity()).map(Variable::value).ifPresent(particleVelocity::set);
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

    public void setParticleVelocity(@NonNull final Vector3fc particleVelocity) {
        this.particleVelocity.set(particleVelocity);
    }

    public void setParticleVelocityDeviation(@NonNull final Vector3fc particleVelocityDeviation) {
        this.particleVelocityDeviation.set(particleVelocityDeviation);
    }

    @Override
    public void setTexture(@NonNull final TextureEntity texture) {
        if (texture instanceof TextureEntityImplementation entity)
            this.texture = entity;
    }
}
