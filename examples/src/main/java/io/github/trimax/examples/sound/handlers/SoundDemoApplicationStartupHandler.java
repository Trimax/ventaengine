package io.github.trimax.examples.sound.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.model.entity.SoundEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SoundDemoApplicationStartupHandler implements VentaEngineStartupHandler {

    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        log.info("=== Sound Demo Started ===");

        try {
            final var soundRegistry = context.getSoundRegistry();

            log.info("Loading sound file: water.ogg");
            final SoundEntity waterSound = soundRegistry.get("water.ogg");

            if (waterSound != null) {
                log.info("Sound loaded successfully!");
                log.info("Sound ID: {}", waterSound.getID());

                if (waterSound instanceof io.github.trimax.venta.engine.model.entity.implementation.SoundEntityImplementation impl) {
                    log.info("Buffer capacity: {} samples", impl.getBuffer().capacity());
                    log.info("Duration: {} seconds", impl.getDuration());
                    log.info("Memory used: ~{} bytes", impl.getBuffer().capacity() * 2);
                }

                log.info("Unloading sound...");
                if (soundRegistry instanceof io.github.trimax.venta.engine.registries.implementation.SoundRegistryImplementation impl) {
                    impl.cleanup();
                    log.info("Sound unloaded successfully!");
                } else {
                    log.warn("Cannot cleanup - registry is not AbstractRegistryImplementation");
                }
            } else {
                log.error("Failed to load sound file!");
            }

        } catch (Exception e) {
            log.error("Error during sound demo: {}", e.getMessage(), e);
        }

        log.info("=== Sound Demo Completed ===");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.exit(0);
    }
}
