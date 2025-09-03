package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.exceptions.UnknownInstanceException;
import io.github.trimax.venta.engine.managers.EmitterManager;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.effects.Particle;
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
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

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
        final var particleVerticesBufferID = memory.getBuffers().create("Emitter %s vertex buffer", name);

        final float[] vertices = {
                // posX, posY
                -0.5f, -0.5f,
                0.5f, -0.5f,
                0.5f,  0.5f,
                -0.5f,  0.5f
        };

        final int[] indices = { 0, 1, 2, 2, 3, 0 };

        glBindVertexArray(particleVertexArrayObjectID);

        glBindBuffer(GL_ARRAY_BUFFER, particleVertexArrayObjectID);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, particleVerticesBufferID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);

        glBindVertexArray(0);

        //TODO: change gizmo type
        final var emitter = abettor.createEmitter(name, programRegistry.get(ProgramType.Particle.getProgramName()), prefab,
                textureRegistry.get(prefab.getDto().texture()), gizmoManager.create("emitter", GizmoType.Light),
                particleVertexArrayObjectID, particleVerticesBufferID);

        for (int particleID = 0; particleID < prefab.getDto().particlesCount(); particleID++)
            emitter.getParticles().add(createParticle(prefab));

        return store(emitter);
    }

    private Particle createParticle(final EmitterPrefabImplementation prefab) {
        final var particle = new Particle(new Vector3f(0), prefab.getDto().velocity(), new Vector3f(0f));

        final var life = 2.f + (float) Math.random();
        particle.setMaxLife(life);
        particle.setLife(life);

        return particle;
    }

    @Override
    public void delete(@NonNull final EmitterInstance instance) {
        if (instance instanceof EmitterInstanceImplementation emitter)
            super.delete(emitter);
    }

    @Override
    protected void destroy(final EmitterInstanceImplementation emitter) {
        log.info("Destroying emitter {} ({})", emitter.getID(), emitter.getName());

        memory.getVertexArrays().delete(emitter.getParticleVertexArrayObjectID());
        memory.getBuffers().delete(emitter.getParticleVerticesBufferID());
    }
}
