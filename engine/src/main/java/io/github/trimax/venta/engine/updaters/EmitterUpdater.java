package io.github.trimax.venta.engine.updaters;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.common.effects.Particle;
import io.github.trimax.venta.engine.model.instance.implementation.EmitterInstanceImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmitterUpdater implements AbstractUpdater<EmitterInstanceImplementation> {
    @Override
    public void update(final EmitterInstanceImplementation emitter, final float delta) {
        final var particlesIterator = emitter.getParticles().iterator();
        while (particlesIterator.hasNext()) {
            final var particle = particlesIterator.next();
            particle.update(delta);

            if (!particle.alive())
                particlesIterator.remove();
        }

        /* Spawning new particles */
        emitter.updateEmissionAccumulator(emitter.getEmissionRate() * delta);
        final var spawnCount = (int) emitter.getEmissionAccumulator();
        emitter.updateEmissionAccumulator(-spawnCount);

        for (int i = 0; i < spawnCount && emitter.getParticles().size() < emitter.getMaximalParticlesCount(); i++)
            emitter.getParticles().add(spawnParticle(emitter));
    }

    private Particle spawnParticle(final EmitterInstanceImplementation emitter) {
        final var velocity = new Vector3f(
                emitter.getVelocity().x + (float)(Math.random() * 2 - 1) * emitter.getDeviation().x,
                emitter.getVelocity().y + (float)(Math.random() * 2 - 1) * emitter.getDeviation().y,
                emitter.getVelocity().z + (float)(Math.random() * 2 - 1) * emitter.getDeviation().z);

        //if (dto.angle() > 0f) {
        //    vel.rotateAxis(
        //            (float) Math.toRadians((Math.random() - 0.5f) * dto.angle()),
        //            dto.direction().x, dto.direction().y, dto.direction().z
        //    );
        //}

        final var particle = new Particle(new Vector3f(emitter.getPosition()),  velocity, new Vector3f(0));

        //TODO: Get correct parameters
        final var life = 4.0f + (float) Math.random();
        particle.setMaxLife(life);
        particle.setLife(life);

        return particle;
    }
}
