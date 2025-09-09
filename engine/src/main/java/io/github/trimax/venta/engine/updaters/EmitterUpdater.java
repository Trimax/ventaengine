package io.github.trimax.venta.engine.updaters;

import org.joml.Vector3f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.common.effects.Particle;
import io.github.trimax.venta.engine.model.instance.implementation.EmitterInstanceImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        final var particle = new Particle(new Vector3f(emitter.getPosition()), velocity, new Vector3f(0));
        particle.setMinimalSize(emitter.getMinimalSize());
        particle.setMaximalSize(emitter.getMaximalSize());

        final var life = emitter.getMinimalLifetime() + (float) Math.random() * emitter.getLifetimeDeviation();
        particle.setMaxLife(life);
        particle.setLife(life);

        particle.setRotation(emitter.getInitialRotationDeviation());
        particle.setAngularVelocity(emitter.getAngularVelocity());

        return particle;
    }
}
