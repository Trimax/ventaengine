package io.github.trimax.venta.engine.registries.implementation;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import io.github.trimax.venta.engine.definitions.Definitions;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.entity.SoundEntity;
import io.github.trimax.venta.engine.model.entity.implementation.Abettor;
import io.github.trimax.venta.engine.model.entity.implementation.SoundEntityImplementation;
import io.github.trimax.venta.engine.registries.SoundRegistry;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SoundRegistryImplementation
        extends AbstractRegistryImplementation<SoundEntityImplementation, SoundEntity, Void>
        implements SoundRegistry {
    private final ResourceService resourceService;
    private final Abettor abettor;

    @Override
    protected SoundEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        return load(resourceService.getAsBytes(String.format("/sounds/%s", resourcePath)));
    }

    private SoundEntityImplementation load(final byte[] data) {
        try (final var info = STBVorbisInfo.malloc()) {
            final ShortBuffer buffer = readVorbis(data, info);
            final float duration = getDuration(buffer, info);

            int bufferId = alGenBuffers();
            int format = info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
            alBufferData(bufferId, format, buffer, Definitions.SOUND_FREQUENCY);

            log.debug("Loaded sound: ({}s, {} channels, {} Hz)", duration, info.channels(), info.sample_rate());

            return abettor.createSound(bufferId, buffer, duration, format);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to load sound: " + e);
        }
    }

    private static float getDuration(final ShortBuffer buffer, final STBVorbisInfo info) {
        final int samples = buffer.limit() / info.channels();
        return (float) samples / info.sample_rate();
    }

    private ShortBuffer readVorbis(@NonNull final byte[] data, @NonNull final STBVorbisInfo info) {
        try (final var stack = MemoryStack.stackPush()) {
            final var audioBuffer = MemoryUtil.memAlloc(data.length);
            audioBuffer.put(data).flip();

            try {
                final IntBuffer error = stack.mallocInt(1);
                final long decoder = stb_vorbis_open_memory(audioBuffer, error, null);

                if (decoder == NULL)
                    throw new RuntimeException("Failed to open Ogg Vorbis data. Error: " + error.get(0));

                try {
                    stb_vorbis_get_info(decoder, info);

                    final int channels = info.channels();
                    final int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

                    final ShortBuffer buffer = MemoryUtil.memAllocShort(lengthSamples * channels);

                    final int samplesRead = stb_vorbis_get_samples_short_interleaved(decoder, channels, buffer);
                    buffer.limit(samplesRead * channels);

                    return buffer;
                } finally {
                    stb_vorbis_close(decoder);
                }
            } finally {
                MemoryUtil.memFree(audioBuffer);
            }
        }
    }

    @Override
    protected void unload(@NonNull final SoundEntityImplementation entity) {
        log.info("Unloading sound {}", entity.getID());

        MemoryUtil.memFree(entity.getBuffer());
    }
}