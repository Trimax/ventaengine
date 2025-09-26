package io.github.trimax.venta.engine.controllers;

import static org.lwjgl.openal.ALC10.*;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.states.AudioState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AudioController extends AbstractController<AudioState, Void> {
    @Override
    protected AudioState create(final Void argument) {
        log.info("Initializing audio system");

        final var deviceID = alcOpenDevice((String) null);
        if (deviceID == 0)
            throw new RuntimeException("Failed to open OpenAL device");

        final var contextID = alcCreateContext(deviceID, (int[]) null);
        if (contextID == 0) {
            alcCloseDevice(deviceID);
            throw new RuntimeException("Failed to create OpenAL context");
        }

        alcMakeContextCurrent(contextID);
        AL.createCapabilities(ALC.createCapabilities(deviceID));

        return new AudioState(contextID, deviceID);
    }

    @Override
    protected void destroy(@NonNull final AudioState state) {
        log.info("Deinitializing audio system");

        alcMakeContextCurrent(0);
        alcDestroyContext(state.getContextID());
        alcCloseDevice(state.getDeviceID());
    }
}
