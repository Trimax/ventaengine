package io.github.trimax.venta.engine.managers.implementation;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL33C.glVertexAttribDivisor;

import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.GeometryDefinitions;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.enums.ParticleLayout;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.exceptions.UnknownInstanceException;
import io.github.trimax.venta.engine.managers.EmitterManager;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.instance.EmitterInstance;
import io.github.trimax.venta.engine.model.instance.implementation.Abettor;
import io.github.trimax.venta.engine.model.instance.implementation.EmitterInstanceImplementation;
import io.github.trimax.venta.engine.model.prefabs.EmitterPrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.EmitterPrefabImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.TextureRegistryImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmitterManagerImplementation
        extends AbstractManagerImplementation<EmitterInstanceImplementation, EmitterInstance>
        implements EmitterManager {
    private final ProgramRegistryImplementation programRegistry;
    private final TextureRegistryImplementation textureRegistry;
    private final GizmoManagerImplementation gizmoManager;
    private final Abettor abettor;
    private final Memory memory;

    @Override
    public EmitterInstanceImplementation create(@NonNull final String name, @NonNull final EmitterPrefab prefab) {
        if (prefab instanceof EmitterPrefabImplementation emitter)
            return create(name, emitter);

        throw new UnknownInstanceException(prefab.getClass());
    }

    private EmitterInstanceImplementation create(@NonNull final String name, @NonNull final EmitterPrefabImplementation prefab) {
        log.info("Loading emitter {}", name);

        final var particleVertexArrayObjectID = memory.getVertexArrays().create("Emitter %s vertex array buffer", name);
        final int particleVerticesBufferID = memory.getBuffers().create("Emitter %s vertex buffer", name);
        final int particleFacesBufferID = memory.getBuffers().create("Emitter %s element buffer", name);

        glBindVertexArray(particleVertexArrayObjectID);

        glBindBuffer(GL_ARRAY_BUFFER, particleVerticesBufferID);
        glBufferData(GL_ARRAY_BUFFER, GeometryDefinitions.PARTICLE_VERTICES, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, particleFacesBufferID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, GeometryDefinitions.PARTICLE_INDICES, GL_STATIC_DRAW);

        glEnableVertexAttribArray(ParticleLayout.Position.getLocationID());
        glVertexAttribPointer(ParticleLayout.Position.getLocationID(), ParticleLayout.Position.getSize(), GL_FLOAT, false, ParticleLayout.Position.getStride(), 0);

        final int particleColorBufferID = createBufferColor(name, prefab);
        final int particleInstanceBufferID = createBufferMatrixModel(name, prefab);

        glBindVertexArray(0);

        return store(abettor.createEmitter(name, programRegistry.get(ProgramType.Particle.getProgramName()), prefab,
                textureRegistry.get(prefab.getDto().texture()), gizmoManager.create("emitter", GizmoType.Emitter),
                MemoryUtil.memAllocFloat(prefab.getDto().particlesCount() * 16),
                MemoryUtil.memAllocFloat(prefab.getDto().particlesCount() * 4),
                particleVertexArrayObjectID, particleVerticesBufferID, particleInstanceBufferID, particleFacesBufferID,
                particleColorBufferID));
    }

    private int createBufferColor(final String name, final EmitterPrefabImplementation prefab) {
        final int particleColorBufferID = memory.getBuffers().create("Emitter %s color buffer", name);
        glBindBuffer(GL_ARRAY_BUFFER, particleColorBufferID);
        glBufferData(GL_ARRAY_BUFFER, (long) prefab.getDto().particlesCount() * ParticleLayout.Color.getStride(), GL_DYNAMIC_DRAW);
        glEnableVertexAttribArray(ParticleLayout.Color.getLocationID());
        glVertexAttribPointer(ParticleLayout.Color.getLocationID(), ParticleLayout.Color.getSize(), GL_FLOAT, false, ParticleLayout.Color.getStride(), 0);
        glVertexAttribDivisor(ParticleLayout.Color.getLocationID(), 1);

        return particleColorBufferID;
    }

    private int createBufferMatrixModel(final String name, final EmitterPrefabImplementation prefab) {
        final int particleInstanceBufferID = memory.getBuffers().create("Emitter %s instance buffer", name);
        glBindBuffer(GL_ARRAY_BUFFER, particleInstanceBufferID);
        glBufferData(GL_ARRAY_BUFFER, (long) prefab.getDto().particlesCount() * ParticleLayout.MatrixModel.getStride(), GL_DYNAMIC_DRAW);
        for (int i = 0; i < 4; i++) {
            glEnableVertexAttribArray(ParticleLayout.MatrixModel.getLocationID() + i);
            glVertexAttribPointer(ParticleLayout.MatrixModel.getLocationID() + i, 4, GL_FLOAT, false, ParticleLayout.MatrixModel.getStride(), (long) i * 4 * Float.BYTES);
            glVertexAttribDivisor(ParticleLayout.MatrixModel.getLocationID() + i, 1);
        }

        return particleInstanceBufferID;
    }

    @Override
    public void delete(@NonNull final EmitterInstance instance) {
        if (instance instanceof EmitterInstanceImplementation emitter)
            super.delete(emitter);
    }

    @Override
    protected void destroy(final EmitterInstanceImplementation emitter) {
        log.info("Destroying emitter {} ({})", emitter.getID(), emitter.getName());

        MemoryUtil.memFree(emitter.getBufferMatrixModel());
        MemoryUtil.memFree(emitter.getBufferColor());

        memory.getVertexArrays().delete(emitter.getParticleVertexArrayObjectID());
        memory.getBuffers().delete(emitter.getParticleInstanceBufferID());
        memory.getBuffers().delete(emitter.getParticleVerticesBufferID());
        memory.getBuffers().delete(emitter.getParticleFacesBufferID());
        memory.getBuffers().delete(emitter.getParticleColorBufferID());
    }
}
