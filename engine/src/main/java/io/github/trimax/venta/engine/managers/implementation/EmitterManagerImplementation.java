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

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        System.out.println("Binding VAO " + particleVertexArrayObjectID + " for matrix attributes");


        final int particleInstanceBufferID = memory.getBuffers().create("Emitter %s instance buffer", name);
        glBindBuffer(GL_ARRAY_BUFFER, particleInstanceBufferID);
        System.out.println("Matrix instance buffer bound: " + particleInstanceBufferID);

        glBufferData(GL_ARRAY_BUFFER, 1000 * 16 * Float.BYTES, GL_DYNAMIC_DRAW);
        for (int i = 0; i < 4; i++) {
            glEnableVertexAttribArray(1 + i);
            glVertexAttribPointer(1 + i, 4, GL_FLOAT, false, 16 * Float.BYTES, (long) i * 4 * Float.BYTES);

//            glVertexAttribPointer(1 + i, 4, GL_FLOAT, false, 16 * Float.BYTES, i * 4 * Float.BYTES);
            glVertexAttribDivisor(1 + i, 1);
        }

        glBindVertexArray(0);

        return store(abettor.createEmitter(name, programRegistry.get(ProgramType.Particle.getProgramName()), prefab,
                textureRegistry.get(prefab.getDto().texture()), gizmoManager.create("emitter", GizmoType.Emitter),
                MemoryUtil.memAllocFloat(prefab.getDto().particlesCount() * 16),
                particleVertexArrayObjectID, particleVerticesBufferID, particleInstanceBufferID, particleFacesBufferID));
    }

    @Override
    public void delete(@NonNull final EmitterInstance instance) {
        if (instance instanceof EmitterInstanceImplementation emitter)
            super.delete(emitter);
    }

    @Override
    protected void destroy(final EmitterInstanceImplementation emitter) {
        log.info("Destroying emitter {} ({})", emitter.getID(), emitter.getName());

        MemoryUtil.memFree(emitter.getModelMatrixBuffer());
        memory.getVertexArrays().delete(emitter.getParticleVertexArrayObjectID());
        memory.getBuffers().delete(emitter.getParticleInstanceBufferID());
        memory.getBuffers().delete(emitter.getParticleVerticesBufferID());
        memory.getBuffers().delete(emitter.getParticleFacesBufferID());
    }
}
