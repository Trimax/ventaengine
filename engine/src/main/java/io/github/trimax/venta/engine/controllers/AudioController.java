package io.github.trimax.venta.engine.controllers;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.SOFTLoopback.*;
import static org.lwjgl.system.MemoryUtil.NULL;

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
    private static final int[] SILENT_DEVICE_ATTRIBUTES = {
            ALC_FORMAT_CHANNELS_SOFT, ALC_STEREO_SOFT,
            ALC_FORMAT_TYPE_SOFT, ALC_SHORT_SOFT,
            ALC_FREQUENCY, 44100,
            0
    };

    @Override
    protected AudioState create(final Void argument) {
        log.info("Initializing audio system");

        var deviceID = alcOpenDevice((String) null);
        int[] attributes = null;
        if (deviceID == 0) {
            log.warn("No audio output device is available. Sound is disabled");

            // A loopback device renders nothing unless asked, but keeps all OpenAL calls valid
            deviceID = openSilentDevice();
            attributes = SILENT_DEVICE_ATTRIBUTES;
        }

        if (deviceID == 0)
            throw new RuntimeException("Failed to open OpenAL device");

        final var contextID = alcCreateContext(deviceID, attributes);
        if (contextID == 0) {
            alcCloseDevice(deviceID);
            throw new RuntimeException("Failed to create OpenAL context");
        }

        alcMakeContextCurrent(contextID);
        AL.createCapabilities(ALC.createCapabilities(deviceID));

        return new AudioState(contextID, deviceID);
    }

    private static long openSilentDevice() {
        if (!alcIsExtensionPresent(NULL, "ALC_SOFT_loopback"))
            return NULL;

        return alcLoopbackOpenDeviceSOFT((CharSequence) null);
    }

    @Override
    protected void destroy(@NonNull final AudioState state) {
        log.info("Deinitializing audio system");

        alcMakeContextCurrent(0);
        alcDestroyContext(state.getContextID());
        alcCloseDevice(state.getDeviceID());
    }
}
