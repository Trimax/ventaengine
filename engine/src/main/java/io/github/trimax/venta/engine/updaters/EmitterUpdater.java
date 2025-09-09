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
                emitter.getParticleVelocity().x + (float)(Math.random() * 2 - 1) * emitter.getParticleVelocityDeviation().x,
                emitter.getParticleVelocity().y + (float)(Math.random() * 2 - 1) * emitter.getParticleVelocityDeviation().y,
                emitter.getParticleVelocity().z + (float)(Math.random() * 2 - 1) * emitter.getParticleVelocityDeviation().z);

        final var particle = new Particle(new Vector3f(emitter.getPosition()), velocity, new Vector3f(0));
        particle.setMinimalSize(emitter.getParticleSize().min());
        particle.setMaximalSize(emitter.getParticleSize().max());

        final var life = emitter.getParticleLifetime().value() + (float) Math.random() * emitter.getParticleLifetime().deviation();
        particle.setMaxLife(life);
        particle.setLife(life);

        particle.setRotation(emitter.getParticleAngularVelocity().deviation());
        particle.setAngularVelocity(emitter.getParticleAngularVelocity().value());

        return particle;
    }
}
